import java.util.*;
public class RandomJava {
	Random r;
	RandomJava() { r = new Random(); }
	public double getRandomValue() { return r.nextDouble(); }
}