package com.sangfor.codescan.sonarqube;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.AbstractLanguage;

/**
 * This class defines the fictive Foo language.
 */
public final class CxxLanguage extends AbstractLanguage {

    public static final String NAME = "C/C++";
    public static final String KEY = "c/c++";
    public static final String FILE_SUFFIXES_PROPERTY_KEY = "sonar.codescan.cxx.file.suffixes";
    public static final String DEFAULT_FILE_SUFFIXES = "cc,c,C,cpp,CPP,hh,h,H";

    private Settings settings;

    public CxxLanguage(Settings settings) {
        super(KEY, NAME);
        this.settings = settings;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getFileSuffixes() {
        String[] suffixes = filterEmptyStrings(settings.getStringArray(FILE_SUFFIXES_PROPERTY_KEY));
        if (suffixes.length == 0) {
            suffixes = StringUtils.split(DEFAULT_FILE_SUFFIXES, ",");
        }
        return suffixes;
    }

    private String[] filterEmptyStrings(String[] stringArray) {
        List<String> nonEmptyStrings = new ArrayList<>();
        for (String string : stringArray) {
            if (StringUtils.isNotBlank(string.trim())) {
                nonEmptyStrings.add(string.trim());
            }
        }
        return nonEmptyStrings.toArray(new String[nonEmptyStrings.size()]);
    }

    /**
     * Allows to know if the given file name has a valid suffix.
     *
     * @param fileName String representing the file name
     * @return boolean <code>true</code> if the file name's suffix is known,
     * <code>false</code> any other way
     */
    public boolean hasValidSuffixes(String fileName) {
        String pathLowerCase = StringUtils.lowerCase(fileName);
        for (String suffix : getFileSuffixes()) {
            if (pathLowerCase.endsWith("." + StringUtils.lowerCase(suffix))) {
                return true;
            }
        }
        return false;
    }

}
