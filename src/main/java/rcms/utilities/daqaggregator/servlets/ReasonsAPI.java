package rcms.utilities.daqaggregator.servlets;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqaggregator.reasoning.base.EventProducer;

public class ReasonsAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(ReasonsAPI.class);

	private List<Entry> data = new ArrayList<>();
	int maxDuration = 1000000;

	ObjectMapper objectMapper = new ObjectMapper();

	public ReasonsAPI() throws JsonProcessingException {
		// random data
		Calendar c = Calendar.getInstance();

		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		df.setTimeZone(tz);

		for (int i = 0; i < 0; i++) {
			Entry object = new Entry();

			Random generator = new Random();
			int randDuration = generator.nextInt(maxDuration/100) + 1;
			int randStart = generator.nextInt(maxDuration) + 1;

			c.setTime(new Date());

			c.add(Calendar.MINUTE, randStart);
			Date startTime = c.getTime();
			c.add(Calendar.SECOND, randDuration);
			Date endTime = c.getTime();
			object.setStart(startTime);
			object.setEnd(endTime);
			object.setContent("test " + i);
			object.setId(i);

			object.calculateDuration();
			data.add(object);
		}
		Collections.sort(data);

	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String startRange = request.getParameter("start");
		String endRange = request.getParameter("end");
		logger.info("Getting reasons from : " + startRange + " to " + endRange);

		Date startDate = objectMapper.readValue(startRange, Date.class);
		Date endDate = objectMapper.readValue(endRange, Date.class);
		logger.info("Parsed range from : " + startDate + " to " + endDate);

		List<Entry> result = new ArrayList<>();
		long diff = endDate.getTime() - startDate.getTime();
		
		Entry gruped = new Entry();
		gruped.setContent("Gruped");
		gruped.setEnd(startDate);
		gruped.setStart(endDate);
		int elementsInRow = 100;
		

		int filtered = 0;
		for (Entry entry : EventProducer.get().getResult()) {
			if(entry.getStart().before(endDate) && entry.getEnd().after(startDate) && entry.isShow()){
				if(entry.getDuration() > diff/elementsInRow ){
					result.add(entry);
				} else {
					filtered++;
					if(entry.getStart().before(gruped.getStart())){
						gruped.setStart(entry.getStart());
					} else if(entry.getEnd().after(gruped.getEnd())){
						gruped.setEnd(entry.getEnd());
					}
				}
			}

		}
		gruped.setContent("Gruped: " + filtered);
		gruped.setGroup("filtered");
		gruped.calculateDuration();
		int k  = 10;
		if(gruped.getDuration() < diff/10){
			Calendar c = Calendar.getInstance();
			c.setTime(gruped.getStart());
			if(diff < Integer.MAX_VALUE)
				c.add(Calendar.MILLISECOND, (int) (diff/k));
			else 
				c.add(Calendar.SECOND, (int) ((diff/1000)/k));
			gruped.setEnd(c.getTime());
		}
		logger.info("Grouped " + filtered + " entries");
		
		if(filtered > 0)
		result.add(gruped);

		String json = objectMapper.writeValueAsString(result);
		// TODO: externalize the Allow-Origin
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD");
		response.addHeader("Access-Control-Allow-Headers",
				"X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept");
		response.addHeader("Access-Control-Max-Age", "1728000");

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);

		logger.info("Response JSON: " + json);

	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}