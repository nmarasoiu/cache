package homework.markers;

/**
 * Classes marked with this mean that they are designed to be used in a thread safe manner.
 */
public @interface ThreadSafe {
   public String comment() default "";
}
