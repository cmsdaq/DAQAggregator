package rcms.utilities.daqaggregator.data.mixin;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;

/**
 * Contains id generators required for serialization.
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

}
