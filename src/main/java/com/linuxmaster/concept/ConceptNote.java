package com.linuxmaster.concept;

import java.util.List;

public class ConceptNote {

    public String id;
    public String category;
    public String title;
    public String summary;
    public List<Section> sections;
    public List<String> relatedQuestionIds;

    public static class Section {
        public String heading;
        public String content;
    }
}
