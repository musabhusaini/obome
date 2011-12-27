package edu.sabanciuniv.dataMining.data.text.opinion;

public class UnratedHotelReview extends Opinion {
	private String hotelId;
	
	public String getHotelId() {
		return hotelId;
	}
	
	public void setHotelId(String hotelId) {
		this.hotelId = hotelId;
	}
	
	@Override
	public String toString() {
		return super.toString() + ", hotel ID: " + this.getHotelId();
	}
}
