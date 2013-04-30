package com.duodeck.workout;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;
import android.content.ContextWrapper;

public class PersistentStorage {

	private String FILENAME = "./hello_file.txt";
	private String string = "hello world!";
	private String data = "";
	
	PersistentStorage() 
	{
	}
	
	public void saveDataToFile() 
	{

		try {
			FileOutputStream fos = new FileOutputStream(FILENAME);
		    Object myInputText = "something";
			fos.write(string.getBytes());
		    fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getDataFromInternalStorage()
	{
		try 
		 {
		    FileInputStream fis = new FileInputStream(FILENAME);
		    DataInputStream in = new DataInputStream(fis);
		    BufferedReader br = new BufferedReader(new InputStreamReader(in));
		    String strLine;
		    
		    while ((strLine = br.readLine()) != null) 
		    {
		    	data = data + strLine;
		    }
		    in.close();
		 } catch (IOException e) {
		    e.printStackTrace();
		 }
		 return data;
	}
}
