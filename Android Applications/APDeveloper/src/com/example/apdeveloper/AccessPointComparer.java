package com.example.apdeveloper;

import java.util.Comparator;

import android.net.wifi.ScanResult;

public class AccessPointComparer implements Comparator<ScanResult>{

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public int compare(ScanResult lhs, ScanResult rhs) {
		 return lhs.level < rhs.level ? 1
		         : lhs.level > rhs.level ? -1
		         : 0;
	}

}
