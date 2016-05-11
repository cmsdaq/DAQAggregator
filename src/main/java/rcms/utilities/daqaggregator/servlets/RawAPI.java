package rcms.utilities.daqaggregator.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqaggregator.TaskManager;
import rcms.utilities.daqaggregator.data.DAQ;

/**
 * Event occurrences servlet API, used for async requests in autoupdate mode.
 * 
 * This API is used by main servlet for event occurrences view.
 * 
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class RawAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;

	ObjectMapper objectMapper = new ObjectMapper();

	private static final Logger logger = Logger.getLogger(RawAPI.class);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String startRange = request.getParameter("start");
		String endRange = request.getParameter("end");
		logger.info("Getting reasons from : " + startRange + " to " + endRange);

		Date startDate = objectMapper.readValue(startRange, Date.class);
		Date endDate = objectMapper.readValue(endRange, Date.class);
		logger.info("Parsed range from : " + startDate + " to " + endDate);

		List<HashMap<String, Long>> data = new ArrayList<>();

		/* iterate over objects in given range */
		for (DAQ daq : TaskManager.get().buf) {
			if (daq.getLastUpdate() >= startDate.getTime() && daq.getLastUpdate() <= endDate.getTime()) {
				HashMap<String, Long> object = new HashMap<>();
				object.put("y", (long) daq.getFedBuilderSummary().getRate());
				object.put("x", daq.getLastUpdate());
				data.add(object);
			}
		}
		
		if(data.size() > 100){
			int factor = data.size() / 100;
			List<HashMap<String,Long>> newdata = new ArrayList<>();
			int curr = 0;
			HashMap<String,Long> candidate = null;
			for(HashMap<String,Long> object : data){
				curr ++;
				if( candidate == null){
					candidate = object;
				} else{
						candidate.put("y", candidate.get("y") + object.get("y"));
					
				}
				
				if(curr == factor){

					candidate.put("y", candidate.get("y") /factor);
					newdata.add(candidate);
					curr = 0;
					candidate = null;
				}
				
			}
			
			logger.info("Reduced from " + data.size() + " to " + newdata.size());
			data = newdata;
			
		}



		ObjectMapper objectMapper = new ObjectMapper();

		String json = objectMapper.writeValueAsString(data);

		// String json = new Gson().toJson(result);

		// logger.info("Response JSON: " + json);

		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD");
		response.addHeader("Access-Control-Allow-Headers",
				"X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept");
		response.addHeader("Access-Control-Max-Age", "1728000");

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		logger.info("Number of elements returned: " + data.size());
		
		response.getWriter().write(json);

	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}