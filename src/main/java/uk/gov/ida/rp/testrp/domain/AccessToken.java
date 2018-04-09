package uk.gov.ida.rp.testrp.domain;

public class AccessToken {
    private final String tokenValue;

    public AccessToken(final String tokenValue) {
        this.tokenValue = tokenValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AccessToken that = (AccessToken) o;

        return tokenValue.equals(that.tokenValue);
    }

    @Override
    public int hashCode() {
        return tokenValue.hashCode();
    }

    @Override
    public String toString() {
        return tokenValue;
    }
}
