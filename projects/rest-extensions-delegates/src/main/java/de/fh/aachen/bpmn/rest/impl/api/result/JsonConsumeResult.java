package de.fh.aachen.bpmn.rest.impl.api.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.fh.aachen.bpmn.rest.ConsumerTaskResult;

public class JsonConsumeResult implements ConsumerTaskResult {

	private String mJsonString = null;

	public JsonConsumeResult(String json) {
		mJsonString = json;
	}

	@Override
	public String getType() {
		return "application/json";
	}

	@Override
	public Object getValue() {
		HashMap<String, Object> resultMap = null;
		if (mJsonString != null) {
			resultMap = parse(mJsonString);
		}
		return resultMap;
	}

	private HashMap<String, Object> parse(String json) {
		JsonParser jsonParser = new JsonParser();
		JsonObject object = (JsonObject) jsonParser.parse(json);
		Set<Map.Entry<String, JsonElement>> set = object.entrySet();
		Iterator<Map.Entry<String, JsonElement>> iterator = set.iterator();
		HashMap<String, Object> map = new HashMap<String, Object>();

		while (iterator.hasNext()) {
			Map.Entry<String, JsonElement> entry = iterator.next();
			String key = entry.getKey();
			JsonElement value = entry.getValue();
			if (!value.isJsonPrimitive() && !value.isJsonNull()) {
				if (value instanceof JsonArray) {
					JsonArray arr = (JsonArray) value;
					Iterator<JsonElement> arrIter = arr.iterator();
					List<Object> list = new ArrayList<Object>();
					while (arrIter.hasNext()) {
						JsonElement next = arrIter.next();
						if (next.isJsonPrimitive()) {
							list.add(next.getAsString());
						} else {
							list.add(parse(next.toString()));
						}
					}
					map.put(key, list);
				} else {
					map.put(key, parse(value.toString()));
				}
			} else {
				if (value instanceof JsonNull) {
					map.put(key, value.toString());
				} else {
					map.put(key, value.getAsString());
				}
			}
		}
		return map;
	}
}