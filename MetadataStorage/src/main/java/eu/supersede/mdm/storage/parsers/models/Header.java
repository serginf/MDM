package eu.supersede.mdm.storage.parsers.models;

import java.util.List;

public class Header {
    List<String> languages;
    List<String> baseIris;
    String iri;
    ElementLangEn title;
    ElementLangEn description;

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public List<String> getBaseIris() {
        return baseIris;
    }

    public void setBaseIris(List<String> baseIris) {
        this.baseIris = baseIris;
    }

    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    public ElementLangEn getTitle() {
        return title;
    }

    public void setTitle(ElementLangEn title) {
        this.title = title;
    }

    public ElementLangEn getDescription() {
        return description;
    }

    public void setDescription(ElementLangEn description) {
        this.description = description;
    }

}
