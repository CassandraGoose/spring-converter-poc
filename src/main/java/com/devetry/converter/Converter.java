package com.devetry.converter;

import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
public class Converter {
	private @Id @GeneratedValue Long id;
	private String date;
	private Boolean successful;

	public Converter() {}
	public Converter(String date, Boolean successful) {
		this.date = date;
		this.successful = successful;
	}

	}
