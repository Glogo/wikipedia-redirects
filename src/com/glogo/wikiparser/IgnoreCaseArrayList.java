package com.glogo.wikiparser;

import java.util.ArrayList;

/**
 * This class is subclass of {@link ArrayList<String>}
 * and enables us to call @{link {@link ArrayList#contains(Object)}
 * with case insensitivity
 */
public class IgnoreCaseArrayList extends ArrayList<String> {
	private static final long serialVersionUID = 6124185757938224205L;

	@Override
    public boolean contains(Object o) {
        String paramStr = (String)o;
        for (String s : this) {
            if (paramStr.equalsIgnoreCase(s)) return true;
        }
        return false;
    }
}