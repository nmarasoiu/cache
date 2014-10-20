package homework.markers;

/**
 * Classes marked with this mean that they are not designed to be used in a thread safe manner.
 */
public @interface NonThreadSafe {
    String comment() default "";
}
