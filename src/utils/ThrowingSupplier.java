package utils;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface ThrowingSupplier<Out> {
	Out supply() throws Exception;

	public static <Out> Optional<Out> tryTimes(int times, ThrowingSupplier<Out> attempt) {
		Supplier<Optional<Out>> catchingSupplier = () -> {
			try {
				return Optional.ofNullable(attempt.supply());
			} catch (Exception e) {
				return Optional.empty();
			}
		};
		return Stream.iterate(catchingSupplier, i -> i).limit(times).map(Supplier::get).filter(Optional::isPresent)
				.findFirst().flatMap(Function.identity());
	}
}