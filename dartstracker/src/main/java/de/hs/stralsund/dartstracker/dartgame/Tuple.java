package de.hs.stralsund.dartstracker.dartgame;

/**
 * small helper class for different purpose
 *
 * @param <A>
 * @param <B>
 */
public class Tuple<A, B> {
    public final A a;
    public final B b;

    public Tuple(A a, B b) {
        this.a = a;
        this.b = b;
    }
}
