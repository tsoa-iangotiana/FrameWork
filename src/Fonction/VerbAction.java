package Fonction;

import java.util.Objects;

public class VerbAction {
    private String url;
    private String verb;

    public VerbAction(String url, String verb) {
        this.url = url;
        this.verb = verb;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VerbAction that = (VerbAction) o;
        return Objects.equals(url, that.url) && Objects.equals(verb, that.verb);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, verb);
    }

    @Override
    public String toString() {
        return "VerbAction{" +
                "url='" + url + '\'' +
                ", verb='" + verb + '\'' +
                '}';
    }
}

