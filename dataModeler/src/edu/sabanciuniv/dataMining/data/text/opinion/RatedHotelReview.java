package edu.sabanciuniv.dataMining.data.text.opinion;

import edu.sabanciuniv.dataMining.data.classification.Classifiable;

public class RatedHotelReview extends UnratedHotelReview implements Classifiable<UnratedHotelReview,Short> {
	private short rating;

	public short getRating() {
		return rating;
	}

	public void setRating(short rating) {
		this.rating = rating;
	}

	@Override
	public Short getClassification() {
//		return new Short((short)(this.getRating() < 3 ? 0 : 1));
		return new Short(this.getRating());
//		return new Short((short)(this.getRating() <= 2 ? 1 : this.getRating() == 3 ? 2 : 3));
	}

	@Override
	public UnratedHotelReview getInstance() {
		return this;
	}
	
	@Override
	public String toString() {
		return super.toString() + ", rating: " + this.getRating();
	}
}