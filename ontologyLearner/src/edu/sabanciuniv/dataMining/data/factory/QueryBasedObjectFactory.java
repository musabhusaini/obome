package edu.sabanciuniv.dataMining.data.factory;

import javax.persistence.TypedQuery;

import edu.sabanciuniv.dataMining.util.LargeTypedQuery;

public class QueryBasedObjectFactory<T> extends AbstractObjectFactory<T> {
	
	protected LargeTypedQuery<T> query;

	protected void setQuery(TypedQuery<T> query) {
		if (query instanceof LargeTypedQuery<?>) {
			this.query = (LargeTypedQuery<T>)query;
		} else {
			this.query = new LargeTypedQuery<>(query, 1000);
		}
	}
	
	public QueryBasedObjectFactory(TypedQuery<T> query) {
		this.setQuery(query);
	}

	public TypedQuery<T> getQuery() {
		return query.getInnerQuery();
	}

	@Override
	public T create() {
		super.create();
		return this.query.getNextResult();
	}
	
	@Override
	public boolean reset() {
		if (super.reset()) {
			this.query.reset();
			return true;
		}
		
		return false;
	}
}