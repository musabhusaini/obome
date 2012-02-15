package edu.sabanciuniv.dataMining.util;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import com.google.common.collect.Lists;

public class LargeTypedQuery<T> implements TypedQuery<T> {

	private TypedQuery<T> innerQuery;
	private int firstResult;
	private int maxResult;
	private int step;
	
	private int cursor;
	private LinkedList<T> buffer;

	private LargeTypedQuery<T> setInnerQuery(TypedQuery<T> innerQuery) {
		this.firstResult = innerQuery.getFirstResult();
		this.maxResult = innerQuery.getMaxResults();
		this.innerQuery = innerQuery;
		this.reset();
		
		return this;
	}
	
	public LargeTypedQuery(TypedQuery<T> innerQuery) {
		this(innerQuery, 1);
	}

	public LargeTypedQuery(TypedQuery<T> innerQuery, int step) {
		this.setInnerQuery(innerQuery)
			.setStep(step);
	}
	
	public T getNextResult() {
		if (this.cursor >= this.firstResult + this.maxResult && this.maxResult != 0) {
			return null;
		}
		
		T result = this.buffer.poll();
		if (result == null) {
			this.innerQuery.setFirstResult(this.cursor)
				.setMaxResults(this.step);
			this.buffer.addAll(this.innerQuery.getResultList());
			if (this.buffer.size() == 0) {
				return null;
			} else {
				return this.getNextResult();
			}
		}
		
		this.cursor++;
		return result;
	}
	
	public TypedQuery<T> getInnerQuery() {
		return this.innerQuery;
	}
	
	public int getStep() {
		return this.step;
	}
	
	public LargeTypedQuery<T> setStep(int step) {
		this.step = step;
		return this;
	}
	
	public LargeTypedQuery<T> reset() {
		this.cursor = this.firstResult;
		this.buffer = Lists.newLinkedList();
		return this;
	}

	@Override
	public int executeUpdate() {
		return this.innerQuery.executeUpdate();
	}

	@Override
	public int getFirstResult() {
		return this.firstResult;
	}

	@Override
	public FlushModeType getFlushMode() {
		return this.innerQuery.getFlushMode();
	}

	@Override
	public Map<String, Object> getHints() {
		return this.innerQuery.getHints();
	}

	@Override
	public LockModeType getLockMode() {
		return this.innerQuery.getLockMode();
	}

	@Override
	public int getMaxResults() {
		return this.maxResult;
	}

	@Override
	public Parameter<?> getParameter(String arg0) {
		return this.innerQuery.getParameter(arg0);
	}

	@Override
	public Parameter<?> getParameter(int arg0) {
		return this.innerQuery.getParameter(arg0);
	}

	@Override
	public <X> Parameter<X> getParameter(String arg0, Class<X> arg1) {
		return this.innerQuery.getParameter(arg0, arg1);
	}

	@Override
	public <X> Parameter<X> getParameter(int arg0, Class<X> arg1) {
		return this.innerQuery.getParameter(arg0, arg1);
	}

	@Override
	public <X> X getParameterValue(Parameter<X> arg0) {
		return this.innerQuery.getParameterValue(arg0);
	}

	@Override
	public Object getParameterValue(String arg0) {
		return this.innerQuery.getParameterValue(arg0);
	}

	@Override
	public Object getParameterValue(int arg0) {
		return this.innerQuery.getParameterValue(arg0);
	}

	@Override
	public Set<Parameter<?>> getParameters() {
		return this.innerQuery.getParameters();
	}

	@Override
	public boolean isBound(Parameter<?> arg0) {
		return this.innerQuery.isBound(arg0);
	}

	@Override
	public <X> X unwrap(Class<X> arg0) {
		return this.innerQuery.unwrap(arg0);
	}

	/**
	 * Of course this should never be used for large queries.
	 */
	@Override
	public List<T> getResultList() {
		this.reset();
		this.cursor = this.firstResult + this.maxResult;
		return this.innerQuery.setFirstResult(this.firstResult)
			.setMaxResults(this.maxResult)
			.getResultList();
	}

	@Override
	public T getSingleResult() {
		return this.innerQuery.getSingleResult();
	}

	@Override
	public LargeTypedQuery<T> setFirstResult(int arg0) {
		this.firstResult = arg0;
		return this;
	}

	@Override
	public LargeTypedQuery<T> setFlushMode(FlushModeType arg0) {
		this.innerQuery.setFlushMode(arg0);
		return this;
	}

	@Override
	public LargeTypedQuery<T> setHint(String arg0, Object arg1) {
		this.innerQuery.setHint(arg0, arg1);
		return this;
	}

	@Override
	public LargeTypedQuery<T> setLockMode(LockModeType arg0) {
		this.innerQuery.setLockMode(arg0);
		return this;
	}

	@Override
	public LargeTypedQuery<T> setMaxResults(int arg0) {
		this.maxResult = arg0;
		return this;
	}

	@Override
	public <X> LargeTypedQuery<T> setParameter(Parameter<X> arg0, X arg1) {
		this.innerQuery.setParameter(arg0, arg1);
		return this;
	}

	@Override
	public LargeTypedQuery<T> setParameter(String arg0, Object arg1) {
		this.innerQuery.setParameter(arg0, arg1);
		return this;
	}

	@Override
	public LargeTypedQuery<T> setParameter(int arg0, Object arg1) {
		this.innerQuery.setParameter(arg0, arg1);
		return this;
	}

	@Override
	public LargeTypedQuery<T> setParameter(Parameter<Calendar> arg0, Calendar arg1, TemporalType arg2) {
		this.innerQuery.setParameter(arg0, arg1, arg2);
		return this;
	}

	@Override
	public LargeTypedQuery<T> setParameter(Parameter<Date> arg0, Date arg1, TemporalType arg2) {
		this.innerQuery.setParameter(arg0, arg1, arg2);
		return this;
	}

	@Override
	public LargeTypedQuery<T> setParameter(String arg0, Calendar arg1, TemporalType arg2) {
		this.innerQuery.setParameter(arg0, arg1, arg2);
		return this;
	}

	@Override
	public LargeTypedQuery<T> setParameter(String arg0, Date arg1, TemporalType arg2) {
		this.innerQuery.setParameter(arg0, arg1, arg2);
		return this;
	}

	@Override
	public LargeTypedQuery<T> setParameter(int arg0, Calendar arg1, TemporalType arg2) {
		this.innerQuery.setParameter(arg0, arg1, arg2);
		return this;
	}

	@Override
	public LargeTypedQuery<T> setParameter(int arg0, Date arg1, TemporalType arg2) {
		this.innerQuery.setParameter(arg0, arg1, arg2);
		return this;
	}
}