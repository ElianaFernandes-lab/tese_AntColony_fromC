package com.ef.antcolony.utils;

import java.lang.reflect.Array;


public class ArrayUtils {

	/**
	 * Deep copies an array of any dimension and any element type.
	 * Supports primitive arrays and object arrays.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T deepCopy(T array) {
		if (array == null) return null;

		Class<?> type = array.getClass();

		if (!type.isArray()) {
			// Base case: not an array → return element directly
			return array;
		}

		int length = Array.getLength(array);
		Class<?> componentType = type.getComponentType();

		// Create a new array of same type and length
		T copy = (T) Array.newInstance(componentType, length);

		// Recursively copy elements
		for (int i = 0; i < length; i++) {
			Object element = Array.get(array, i);
			Object copiedElement = deepCopy(element);
			Array.set(copy, i, copiedElement);
		}

		return copy;
	}

	public static String arrayToString(Object array) {
		if (array == null) return "null";

		Class<?> cls = array.getClass();

		// If it's not an array → just return toString()
		if (!cls.isArray()) {
			return array.toString();
		}

		int length = Array.getLength(array);
		StringBuilder sb = new StringBuilder();
		sb.append("[");

		for (int i = 0; i < length; i++) {
			Object element = Array.get(array, i);

			// Recursively print element
			sb.append(arrayToString(element));

			if (i < length - 1) sb.append(", ");
		}

		sb.append("]");
		return sb.toString();
	}
}

