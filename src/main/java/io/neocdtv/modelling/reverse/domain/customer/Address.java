/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.neocdtv.modelling.reverse.domain.customer;

/**
 * @author xix
 */
public class Address extends Object{
	private int street;
	private int city;
	private Person person;

	public int getStreet() {
		return street;
	}

	public void setStreet(int street) {
		this.street = street;
	}

	public int getCity() {
		return city;
	}

	public void setCity(int city) {
		this.city = city;
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}


}