package rcms.utilities.daqaggregator.data.mixin.ref;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;

import rcms.utilities.daqaggregator.data.BU;
import rcms.utilities.daqaggregator.data.BUSummary;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.data.FEDBuilderSummary;
import rcms.utilities.daqaggregator.data.FMM;
import rcms.utilities.daqaggregator.data.FMMApplication;
import rcms.utilities.daqaggregator.data.FRL;
import rcms.utilities.daqaggregator.data.FRLPc;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.data.SubFEDBuilder;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqaggregator.mappers.helper.ContextHelper;

/**
 * Contains id generators required for serialization.
 * 
 * @author Philipp Maximilian Brummer (philipp.maximilian.brummer@cern.ch)
 * @author Michail Vougioukas (michail.vougioukas@cern.ch)
 * 
 */
public class IdGenerators {

	/**
	 * Copied from Jackson's ObjectIdGenerators.Base<T>
	 *
	 * @param <T>
	 *            Type of the id to generate.
	 * 
	 */
	@SuppressWarnings("serial")
	private abstract static class Base<T> extends ObjectIdGenerator<T> {
		protected final Class<?> _scope;

		protected Base(Class<?> scope) {
			_scope = scope;
		}

		@Override
		public final Class<?> getScope() {
			return _scope;
		}

		@Override
		public boolean canUseFor(ObjectIdGenerator<?> gen) {
			return (gen.getClass() == getClass()) && (gen.getScope() == _scope);
		}

		@Override
		public abstract T generateId(Object forPojo);
	}

	/**
	 * Behaves like Jackson's ObjectIdGenerators.IntSequenceGenerator, but
	 * remembers objects it already encountered and assigns them the same id
	 * that was generated for them when first encountered. New IDs are generated
	 * sequentially.
	 */
	public final static class ObjectUniqueIntIdGenerator extends Base<Integer> {
		private static final long serialVersionUID = 1L;

		protected transient Map<Object, Integer> knownObjects;
		protected transient int _nextValue;

		public ObjectUniqueIntIdGenerator() {
			this(Object.class, -1);
		}

		public ObjectUniqueIntIdGenerator(Class<?> scope, int fv) {
			super(scope);
			knownObjects = new HashMap<>();
			_nextValue = fv;
		}

		protected int initialValue() {
			return 1;
		}

		@Override
		public ObjectIdGenerator<Integer> forScope(Class<?> scope) {
			return (_scope == scope) ? this : new ObjectUniqueIntIdGenerator(scope, _nextValue);
		}

		@Override
		public ObjectIdGenerator<Integer> newForSerialization(Object context) {
			return new ObjectUniqueIntIdGenerator(_scope, initialValue());
		}

		@Override
		public IdKey key(Object key) {
			if (key == null) {
				return null;
			}
			return new IdKey(getClass(), _scope, key);
		}

		@Override
		public Integer generateId(Object forPojo) {
			if (forPojo == null) {
				return null;
			}

			int id;

			Integer existingKey = knownObjects.get(forPojo);
			if (existingKey != null) {
				id = existingKey;
			} else {
				id = _nextValue;
				++_nextValue;
				knownObjects.put(forPojo, id);
			}

			return id;
		}
	}
	
	 public final static class BUIdGenerator extends Base<String> {
	        private static final String prefix = "BU_";
	       
	        private static final long serialVersionUID = 1L;

	        public BUIdGenerator() {
	            this(Object.class);
	        }

	        public BUIdGenerator(Class<?> scope) {
	            super(scope);
	        }

	        @Override
	        public ObjectIdGenerator<String> forScope(Class<?> scope) {
	            return (_scope == scope) ? this : new BUIdGenerator(scope);
	        }

	        @Override
	        public ObjectIdGenerator<String> newForSerialization(Object context) {
	            return new BUIdGenerator(_scope);
	        }

	        @Override
	        public IdKey key(Object key) {
	            if (key == null) {
	                return null;
	            }
	            return new IdKey(getClass(), _scope, key);
	        }

	        @Override
	        public String generateId(Object forPojo) {
	            if (forPojo == null) {
	                return null;
	            } else if (!(forPojo instanceof BU)) {
	                return null;
	            }
	           
	            BU bu = (BU) forPojo;

	            String buHostname = bu.getHostname();
	            buHostname = ContextHelper.removeSuffixFromHostname(buHostname, ".cms");
	           
	            StringBuilder sb = new StringBuilder(prefix.length() + buHostname.length());
	           
	            sb.append(prefix);
	            sb.append(buHostname);
	           
	            return sb.toString();
	        }
	    }
	 
	 public final static class RUIdGenerator extends Base<String> {
	        private static final String prefix = "RU_";
	       
	        private static final long serialVersionUID = 1L;

	        public RUIdGenerator() {
	            this(Object.class);
	        }

	        public RUIdGenerator(Class<?> scope) {
	            super(scope);
	        }

	        @Override
	        public ObjectIdGenerator<String> forScope(Class<?> scope) {
	            return (_scope == scope) ? this : new RUIdGenerator(scope);
	        }

	        @Override
	        public ObjectIdGenerator<String> newForSerialization(Object context) {
	            return new RUIdGenerator(_scope);
	        }

	        @Override
	        public IdKey key(Object key) {
	            if (key == null) {
	                return null;
	            }
	            return new IdKey(getClass(), _scope, key);
	        }

	        @Override
	        public String generateId(Object forPojo) {
	            if (forPojo == null) {
	                return null;
	            } else if (!(forPojo instanceof RU)) {
	                return null;
	            }
	           
	            RU ru = (RU) forPojo;

	            String ruHostname = ru.getHostname();
	            ruHostname = ContextHelper.removeSuffixFromHostname(ruHostname, ".cms");
	           
	            StringBuilder sb = new StringBuilder(prefix.length() + ruHostname.length());
	           
	            sb.append(prefix);
	            sb.append(ruHostname);
	           
	            return sb.toString();
	        }
	    }
	 
	 public final static class FRLPcIdGenerator extends Base<String> {
	        private static final String prefix = "FRLPC_";
	       
	        private static final long serialVersionUID = 1L;

	        public FRLPcIdGenerator() {
	            this(Object.class);
	        }

	        public FRLPcIdGenerator(Class<?> scope) {
	            super(scope);
	        }

	        @Override
	        public ObjectIdGenerator<String> forScope(Class<?> scope) {
	            return (_scope == scope) ? this : new FRLPcIdGenerator(scope);
	        }

	        @Override
	        public ObjectIdGenerator<String> newForSerialization(Object context) {
	            return new FRLPcIdGenerator(_scope);
	        }

	        @Override
	        public IdKey key(Object key) {
	            if (key == null) {
	                return null;
	            }
	            return new IdKey(getClass(), _scope, key);
	        }

	        @Override
	        public String generateId(Object forPojo) {
	            if (forPojo == null) {
	                return null;
	            } else if (!(forPojo instanceof FRLPc)) {
	                return null;
	            }
	           
	            FRLPc frlpc = (FRLPc) forPojo;

	            String frlpcHostname = frlpc.getHostname();
	            frlpcHostname = ContextHelper.removeSuffixFromHostname(frlpcHostname, ".cms");
	           
	            StringBuilder sb = new StringBuilder(prefix.length() + frlpcHostname.length());
	           
	            sb.append(prefix);
	            sb.append(frlpcHostname);
	           
	            return sb.toString();
	        }
	    }
	 
	 public final static class FEDIdGenerator extends Base<String> {
	        private static final String prefix = "FED_";
	       
	        private static final long serialVersionUID = 1L;

	        public FEDIdGenerator() {
	            this(Object.class);
	        }

	        public FEDIdGenerator(Class<?> scope) {
	            super(scope);
	        }

	        @Override
	        public ObjectIdGenerator<String> forScope(Class<?> scope) {
	            return (_scope == scope) ? this : new FEDIdGenerator(scope);
	        }

	        @Override
	        public ObjectIdGenerator<String> newForSerialization(Object context) {
	            return new FEDIdGenerator(_scope);
	        }

	        @Override
	        public IdKey key(Object key) {
	            if (key == null) {
	                return null;
	            }
	            return new IdKey(getClass(), _scope, key);
	        }

	        @Override
	        public String generateId(Object forPojo) {
	            if (forPojo == null) {
	                return null;
	            } else if (!(forPojo instanceof FED)) {
	                return null;
	            }
	           
	            FED fed = (FED) forPojo;

	            String fedHwcfgDBid = String.valueOf(fed.getId());
	           
	            StringBuilder sb = new StringBuilder(prefix.length() + fedHwcfgDBid.length());
	           
	            sb.append(prefix);
	            sb.append(fedHwcfgDBid);
	           
	            return sb.toString();
	        }
	    }
	 
	 public final static class SubSystemIdGenerator extends Base<String> {
	        private static final String prefix = "SS_";
	       
	        private static final long serialVersionUID = 1L;

	        public SubSystemIdGenerator() {
	            this(Object.class);
	        }

	        public SubSystemIdGenerator(Class<?> scope) {
	            super(scope);
	        }

	        @Override
	        public ObjectIdGenerator<String> forScope(Class<?> scope) {
	            return (_scope == scope) ? this : new SubSystemIdGenerator(scope);
	        }

	        @Override
	        public ObjectIdGenerator<String> newForSerialization(Object context) {
	            return new SubSystemIdGenerator(_scope);
	        }

	        @Override
	        public IdKey key(Object key) {
	            if (key == null) {
	                return null;
	            }
	            return new IdKey(getClass(), _scope, key);
	        }

	        @Override
	        public String generateId(Object forPojo) {
	            if (forPojo == null) {
	                return null;
	            } else if (!(forPojo instanceof SubSystem)) {
	                return null;
	            }
	           
	            SubSystem subsys = (SubSystem) forPojo;

	            String subsysName = subsys.getName();
	           
	            StringBuilder sb = new StringBuilder(prefix.length() + subsysName.length());
	           
	            sb.append(prefix);
	            sb.append(subsysName);
	           
	            return sb.toString();
	        }
	    }
	 
	 public final static class TTCPartitionIdGenerator extends Base<String> {
	        private static final String prefix = "TTCP_";
	       
	        private static final long serialVersionUID = 1L;

	        public TTCPartitionIdGenerator() {
	            this(Object.class);
	        }

	        public TTCPartitionIdGenerator(Class<?> scope) {
	            super(scope);
	        }

	        @Override
	        public ObjectIdGenerator<String> forScope(Class<?> scope) {
	            return (_scope == scope) ? this : new TTCPartitionIdGenerator(scope);
	        }

	        @Override
	        public ObjectIdGenerator<String> newForSerialization(Object context) {
	            return new TTCPartitionIdGenerator(_scope);
	        }

	        @Override
	        public IdKey key(Object key) {
	            if (key == null) {
	                return null;
	            }
	            return new IdKey(getClass(), _scope, key);
	        }

	        @Override
	        public String generateId(Object forPojo) {
	            if (forPojo == null) {
	                return null;
	            } else if (!(forPojo instanceof TTCPartition)) {
	                return null;
	            }
	           
	            TTCPartition ttcp = (TTCPartition) forPojo;

	            String ttcpName = ttcp.getName();
	           
	            StringBuilder sb = new StringBuilder(prefix.length() + ttcpName.length());
	           
	            sb.append(prefix);
	            sb.append(ttcpName);
	           
	            return sb.toString();
	        }
	    }
	 
	 public final static class FEDBuilderIdGenerator extends Base<String> {
	        private static final String prefix = "FEDB_";
	       
	        private static final long serialVersionUID = 1L;

	        public FEDBuilderIdGenerator() {
	            this(Object.class);
	        }

	        public FEDBuilderIdGenerator(Class<?> scope) {
	            super(scope);
	        }

	        @Override
	        public ObjectIdGenerator<String> forScope(Class<?> scope) {
	            return (_scope == scope) ? this : new FEDBuilderIdGenerator(scope);
	        }

	        @Override
	        public ObjectIdGenerator<String> newForSerialization(Object context) {
	            return new FEDBuilderIdGenerator(_scope);
	        }

	        @Override
	        public IdKey key(Object key) {
	            if (key == null) {
	                return null;
	            }
	            return new IdKey(getClass(), _scope, key);
	        }

	        @Override
	        public String generateId(Object forPojo) {
	            if (forPojo == null) {
	                return null;
	            } else if (!(forPojo instanceof FEDBuilder)) {
	                return null;
	            }
	           
	            FEDBuilder fedbuilder = (FEDBuilder) forPojo;

	            String fedbuilderName = fedbuilder.getName();
	           
	            StringBuilder sb = new StringBuilder(prefix.length() + fedbuilderName.length());
	           
	            sb.append(prefix);
	            sb.append(fedbuilderName);
	           
	            return sb.toString();
	        }
	    }
	 
	 public final static class FMMApplicationIdGenerator extends Base<String> {
	        private static final String prefix = "FMMA_";
	       
	        private static final long serialVersionUID = 1L;

	        public FMMApplicationIdGenerator() {
	            this(Object.class);
	        }

	        public FMMApplicationIdGenerator(Class<?> scope) {
	            super(scope);
	        }

	        @Override
	        public ObjectIdGenerator<String> forScope(Class<?> scope) {
	            return (_scope == scope) ? this : new FMMApplicationIdGenerator(scope);
	        }

	        @Override
	        public ObjectIdGenerator<String> newForSerialization(Object context) {
	            return new FMMApplicationIdGenerator(_scope);
	        }

	        @Override
	        public IdKey key(Object key) {
	            if (key == null) {
	                return null;
	            }
	            return new IdKey(getClass(), _scope, key);
	        }

	        @Override
	        public String generateId(Object forPojo) {
	            if (forPojo == null) {
	                return null;
	            } else if (!(forPojo instanceof FMMApplication)) {
	                return null;
	            }
	           
	            FMMApplication fmmapp = (FMMApplication) forPojo;

	            String fmmappHostname = fmmapp.getHostname();
	            fmmappHostname = ContextHelper.removeSuffixFromHostname(fmmappHostname, ".cms");
	           
	            StringBuilder sb = new StringBuilder(prefix.length() + fmmappHostname.length());
	           
	            sb.append(prefix);
	            sb.append(fmmappHostname);
	           
	            return sb.toString();
	        }
	    }
	 
	 public final static class FMMIdGenerator extends Base<String> {
	        private static final String prefix = "FMM_";
	       
	        private static final long serialVersionUID = 1L;

	        public FMMIdGenerator() {
	            this(Object.class);
	        }

	        public FMMIdGenerator(Class<?> scope) {
	            super(scope);
	        }

	        @Override
	        public ObjectIdGenerator<String> forScope(Class<?> scope) {
	            return (_scope == scope) ? this : new FMMIdGenerator(scope);
	        }

	        @Override
	        public ObjectIdGenerator<String> newForSerialization(Object context) {
	            return new FMMIdGenerator(_scope);
	        }

	        @Override
	        public IdKey key(Object key) {
	            if (key == null) {
	                return null;
	            }
	            return new IdKey(getClass(), _scope, key);
	        }

	        @Override
	        public String generateId(Object forPojo) {
	            if (forPojo == null) {
	                return null;
	            } else if (!(forPojo instanceof FMM)) {
	                return null;
	            }
	           
	            
	            FMM fmm = (FMM) forPojo;
	            
	            String fmmHwcfgDBId = fmm.getId();
	            
	            StringBuilder sb = new StringBuilder(prefix.length() + fmmHwcfgDBId.length());
	           
	            sb.append(prefix);
	            sb.append(fmmHwcfgDBId);
	            
	            return sb.toString();
	            
	        }
	    }
	 
	 public final static class SubFEDBuilderIdGenerator extends Base<String> {
	        private static final String prefix = "SFB_";
	        private static final String delimiter = "$";
	       
	        private static final long serialVersionUID = 1L;

	        public SubFEDBuilderIdGenerator() {
	            this(Object.class);
	        }

	        public SubFEDBuilderIdGenerator(Class<?> scope) {
	            super(scope);
	        }

	        @Override
	        public ObjectIdGenerator<String> forScope(Class<?> scope) {
	            return (_scope == scope) ? this : new SubFEDBuilderIdGenerator(scope);
	        }

	        @Override
	        public ObjectIdGenerator<String> newForSerialization(Object context) {
	            return new SubFEDBuilderIdGenerator(_scope);
	        }

	        @Override
	        public IdKey key(Object key) {
	            if (key == null) {
	                return null;
	            }
	            return new IdKey(getClass(), _scope, key);
	        }

	        @Override
	        public String generateId(Object forPojo) {
	            if (forPojo == null) {
	                return null;
	            } else if (!(forPojo instanceof SubFEDBuilder)) {
	                return null;
	            }
	           
	            SubFEDBuilder sfb = (SubFEDBuilder) forPojo;

	            String fedBuilderName = sfb.getFedBuilder().getName();
	            String ttcpName = sfb.getTtcPartition().getName();
				String frlpcHostname = sfb.getFrlPc() != null ? sfb.getFrlPc().getHostname() : "-";
	            frlpcHostname = ContextHelper.removeSuffixFromHostname(frlpcHostname, ".cms");

	            StringBuilder sb = new StringBuilder(prefix.length() + fedBuilderName.length() + delimiter.length() + ttcpName.length() + delimiter.length() + frlpcHostname.length());
	           
	            sb.append(prefix);
	            sb.append(fedBuilderName);
	            sb.append(delimiter);
	            sb.append(ttcpName);
	            sb.append(delimiter);
	            sb.append(frlpcHostname);
	           
	            return sb.toString();
	        }
	    }
	 
	 public final static class FRLIdGenerator extends Base<String> {
	        private static final String prefix = "FRL_";
	        private static final String delimiter = "$";
	       
	        private static final long serialVersionUID = 1L;

	        public FRLIdGenerator() {
	            this(Object.class);
	        }

	        public FRLIdGenerator(Class<?> scope) {
	            super(scope);
	        }

	        @Override
	        public ObjectIdGenerator<String> forScope(Class<?> scope) {
	            return (_scope == scope) ? this : new FRLIdGenerator(scope);
	        }

	        @Override
	        public ObjectIdGenerator<String> newForSerialization(Object context) {
	            return new FRLIdGenerator(_scope);
	        }

	        @Override
	        public IdKey key(Object key) {
	            if (key == null) {
	                return null;
	            }
	            return new IdKey(getClass(), _scope, key);
	        }

	        @Override
	        public String generateId(Object forPojo) {
	            if (forPojo == null) {
	                return null;
	            } else if (!(forPojo instanceof FRL)) {
	                return null;
	            }
	           
	            FRL frl = (FRL) forPojo;

	            String frlGeoslot = String.valueOf(frl.getGeoSlot());
	            
	            String frlpcHostname = frl.getFrlPc().getHostname();
	            frlpcHostname = ContextHelper.removeSuffixFromHostname(frlpcHostname, ".cms");
	            
	            StringBuilder sb = new StringBuilder(prefix.length() + frlGeoslot.length() + delimiter.length() + frlpcHostname.length());
	           
	            sb.append(prefix);
	            sb.append(frlGeoslot);
	            sb.append(delimiter);
	            sb.append(frlpcHostname);
	            
	            
	            return sb.toString();
	        }
	    }
	 
	 public final static class DAQIdGenerator extends Base<String> {
	        private static final String prefix = "DAQ";
	       
	        private static final long serialVersionUID = 1L;

	        public DAQIdGenerator() {
	            this(Object.class);
	        }

	        public DAQIdGenerator(Class<?> scope) {
	            super(scope);
	        }

	        @Override
	        public ObjectIdGenerator<String> forScope(Class<?> scope) {
	            return (_scope == scope) ? this : new DAQIdGenerator(scope);
	        }

	        @Override
	        public ObjectIdGenerator<String> newForSerialization(Object context) {
	            return new DAQIdGenerator(_scope);
	        }

	        @Override
	        public IdKey key(Object key) {
	            if (key == null) {
	                return null;
	            }
	            return new IdKey(getClass(), _scope, key);
	        }

	        @Override
	        public String generateId(Object forPojo) {
	            if (forPojo == null) {
	                return null;
	            } else if (!(forPojo instanceof DAQ)) {
	                return null;
	            }
	           
	            StringBuilder sb = new StringBuilder(prefix.length());
	           
	            sb.append(prefix);
	           
	            return sb.toString();
	        }
	    }
	 
	 public final static class FEDBuilderSummaryIdGenerator extends Base<String> {
	        private static final String prefix = "FEDBS";
	       
	        private static final long serialVersionUID = 1L;

	        public FEDBuilderSummaryIdGenerator() {
	            this(Object.class);
	        }

	        public FEDBuilderSummaryIdGenerator(Class<?> scope) {
	            super(scope);
	        }

	        @Override
	        public ObjectIdGenerator<String> forScope(Class<?> scope) {
	            return (_scope == scope) ? this : new FEDBuilderSummaryIdGenerator(scope);
	        }

	        @Override
	        public ObjectIdGenerator<String> newForSerialization(Object context) {
	            return new FEDBuilderSummaryIdGenerator(_scope);
	        }

	        @Override
	        public IdKey key(Object key) {
	            if (key == null) {
	                return null;
	            }
	            return new IdKey(getClass(), _scope, key);
	        }

	        @Override
	        public String generateId(Object forPojo) {
	            if (forPojo == null) {
	                return null;
	            } else if (!(forPojo instanceof FEDBuilderSummary)) {
	                return null;
	            }
	           
	            StringBuilder sb = new StringBuilder(prefix.length());
	           
	            sb.append(prefix);
	           
	            return sb.toString();
	        }
	    }
	 
	 public final static class BUSummaryIdGenerator extends Base<String> {
	        private static final String prefix = "BUS";
	       
	        private static final long serialVersionUID = 1L;

	        public BUSummaryIdGenerator() {
	            this(Object.class);
	        }

	        public BUSummaryIdGenerator(Class<?> scope) {
	            super(scope);
	        }

	        @Override
	        public ObjectIdGenerator<String> forScope(Class<?> scope) {
	            return (_scope == scope) ? this : new BUSummaryIdGenerator(scope);
	        }

	        @Override
	        public ObjectIdGenerator<String> newForSerialization(Object context) {
	            return new BUSummaryIdGenerator(_scope);
	        }

	        @Override
	        public IdKey key(Object key) {
	            if (key == null) {
	                return null;
	            }
	            return new IdKey(getClass(), _scope, key);
	        }

	        @Override
	        public String generateId(Object forPojo) {
	            if (forPojo == null) {
	                return null;
	            } else if (!(forPojo instanceof BUSummary)) {
	                return null;
	            }
	           
	            StringBuilder sb = new StringBuilder(prefix.length());
	           
	            sb.append(prefix);
	           
	            return sb.toString();
	        }
	    }
}
