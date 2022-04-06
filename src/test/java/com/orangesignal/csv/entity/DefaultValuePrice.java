/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.orangesignal.csv.entity;

import java.util.Date;

import com.orangesignal.csv.annotation.CsvColumn;
import com.orangesignal.csv.annotation.CsvColumns;
import com.orangesignal.csv.annotation.CsvEntity;

/**
 * @author Koji Sugisawa
 */
@CsvEntity(header = true)
public class DefaultValuePrice {

	public DefaultValuePrice() {}

	public DefaultValuePrice(String symbol, String name, Number price, Number volume, Date date) {
		this.symbol = symbol;
		this.name = name;
		this.date = date;
		this.price = price;
		this.volume = volume;
	}

	@CsvColumn(position = 0, name = "シンボル", required = true, defaultValue = "XXXX")
	public String symbol;

	@CsvColumns({
		@CsvColumn(position = 4, name = "日付", format = "yyyy/MM/dd", language = "ja", country = "JP", timezone = "Asia/Tokyo", defaultValue = "2014/02/02"),
		@CsvColumn(position = 5, name = "時刻", format = "HH:mm:ss", language = "ja", country = "JP", timezone = "Asia/Tokyo", defaultValue = "12:00:00")
	})
	public Date date;

	@CsvColumn(position = 1, name = "名称", defaultValue = "xxx")
	public String name;

	@CsvColumn(position = 2, name = "価格", format = "#,##0", language = "ja", country = "JP")
	public Number price;

	@CsvColumn(position = 3, name = "出来高")
	public Number volume;

}