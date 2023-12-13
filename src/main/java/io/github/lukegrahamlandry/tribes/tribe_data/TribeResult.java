package io.github.lukegrahamlandry.tribes.tribe_data;

import com.mojang.datafixers.util.Either;

public class TribeResult<T> {

    private final Either<T, TribeError> result;

    private TribeResult(Either<T, TribeError> result) {
        this.result = result;
    }

    public static <T> TribeResult<T> success(T result) {
        return new TribeResult<>(Either.left(result));
    }

    public static TribeResult<Void> empty_success() {
        return success(null);
    }

    public static <T> TribeResult<T> error(TribeError error) {
        return new TribeResult<>(Either.right(error));
    }

    public Either<T, TribeError> asEither() {
        return this.result;
    }

    public boolean success() {
        return this.result.left().isPresent();
    }

    public boolean failed() {
        return this.result.right().isPresent();
    }

    public T value() {
        return this.result.orThrow();
    }

    public TribeError error() {
        return this.result.right().orElseThrow();
    }
}
