package gautamhans.xyz.paginationtmdb.models;

/**
 * Created by Gautam on 22-Jul-17.
 */

public class MovieDetails {

    private String MOVIE_TITLE, MOVIE_TAG_LINE, MOVIE_RELEASE, MOVIE_SYNOPSIS;
    private float MOVIE_RATING;
    private byte[] MOVIE_POSTER;

    public MovieDetails(String MOVIE_TITLE, String MOVIE_TAG_LINE, String MOVIE_RELEASE, String MOVIE_SYNOPSIS, float MOVIE_RATING, byte[] MOVIE_POSTER) {
        this.MOVIE_TITLE = MOVIE_TITLE;
        this.MOVIE_TAG_LINE = MOVIE_TAG_LINE;
        this.MOVIE_RELEASE = MOVIE_RELEASE;
        this.MOVIE_SYNOPSIS = MOVIE_SYNOPSIS;
        this.MOVIE_RATING = MOVIE_RATING;
        this.MOVIE_POSTER = MOVIE_POSTER;
    }

    public String getMOVIE_TITLE() {
        return MOVIE_TITLE;
    }

    public void setMOVIE_TITLE(String MOVIE_TITLE) {
        this.MOVIE_TITLE = MOVIE_TITLE;
    }

    public String getMOVIE_TAG_LINE() {
        return MOVIE_TAG_LINE;
    }

    public void setMOVIE_TAG_LINE(String MOVIE_TAG_LINE) {
        this.MOVIE_TAG_LINE = MOVIE_TAG_LINE;
    }

    public String getMOVIE_RELEASE() {
        return MOVIE_RELEASE;
    }

    public void setMOVIE_RELEASE(String MOVIE_RELEASE) {
        this.MOVIE_RELEASE = MOVIE_RELEASE;
    }

    public String getMOVIE_SYNOPSIS() {
        return MOVIE_SYNOPSIS;
    }

    public void setMOVIE_SYNOPSIS(String MOVIE_SYNOPSIS) {
        this.MOVIE_SYNOPSIS = MOVIE_SYNOPSIS;
    }

    public float getMOVIE_RATING() {
        return MOVIE_RATING;
    }

    public void setMOVIE_RATING(float MOVIE_RATING) {
        this.MOVIE_RATING = MOVIE_RATING;
    }

    public byte[] getMOVIE_POSTER() {
        return MOVIE_POSTER;
    }

    public void setMOVIE_POSTER(byte[] MOVIE_POSTER) {
        this.MOVIE_POSTER = MOVIE_POSTER;
    }

}
