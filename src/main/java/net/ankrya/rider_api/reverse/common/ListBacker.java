package net.ankrya.rider_api.reverse.common;

public class ListBacker implements IBacker {

	public final IBacker[] backers;
	
	public ListBacker(IBacker[] backers) {
		this.backers = backers;
	}
	
	@Override
	public void back() {
		for (int i = 0; i < backers.length; i++) {
			IBacker iBacker = backers[i];
			iBacker.back();
		}
	}

}
