package gtranslator.domain.validator.base;

public interface Validator<T> {
    boolean isValid(T arg);
}
