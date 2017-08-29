import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.AbstractTypeResolver;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * A repro of an annotation introspection issue in jackson 2.8.8.
 *
 * Breaks: mvn -Djackson.version=2.8.8 compile exec:java -Dexec.mainClass=Repro
 *
 * Works: mvn -Djackson.version=2.8.6 compile exec:java -Dexec.mainClass=Repro
 *
 * Additionally, commenting out either {@code @JsonIgnore} or {@code @JsonDeserialize} annotations on
 * {@link Foobar#foo()} or {@link Foobar#bar()} respectively unbreaks deserialization for jackson 2.8.8.
 */
public class Repro {

  /**
   * An abstract type to deserialize.
   */
  public interface Foobar {

    @JsonIgnore // Comment out this annotation or ...
    String foo();

    @JsonDeserialize(using = CustomStringDeserializer.class) // ... this annotation to unbreak deserialization.
    String bar();
  }

  /**
   * Custom String deserializer.
   */
  private static class CustomStringDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      return p.getText();
    }
  }

  /**
   * A concrete implementation.
   */
  public static class FoobarImpl implements Foobar {

    private final String foo;
    private final String bar;

    public FoobarImpl(@Field("foo") String foo, @Field("bar") String bar) {
      this.foo = foo;
      this.bar = bar;
    }

    @Override
    public String foo() {
      return this.foo;
    }

    @Override
    public String bar() {
      return this.bar;
    }

    @Override
    public String toString() {
      return "FoobarImpl{" + "foo='" + foo + '\'' + ", bar='" + bar + '\'' + '}';
    }
  }

  /**
   * Instructs jackson to instantiate {@link FoobarImpl} when deserializing {@link Foobar}.
   */
  public static class FoobarAbstractTypeResolver extends AbstractTypeResolver {

    @Override
    public JavaType resolveAbstractType(DeserializationConfig config, BeanDescription typeDesc) {
      if (typeDesc.getBeanClass() == Foobar.class) {
        return config.getTypeFactory().constructType(FoobarImpl.class);
      }
      return super.resolveAbstractType(config, typeDesc);
    }
  }

  @Target({PARAMETER, METHOD})
  @Retention(RUNTIME)
  @interface Field {

    String value() default "";
  }

  /**
   * Instructs jackson that {@link Foobar#foo()}, {@link Foobar#bar()} and the {@code foo} and {@code bar} constructor
   * arguments map to the {@code foo} and {@code bar} properties.
   */
  public static class FoobarAnnotationIntrospector extends NopAnnotationIntrospector {

    @Override
    public String findImplicitPropertyName(final AnnotatedMember member) {
      // Constructor parameter
      if (member instanceof AnnotatedParameter) {
        final Field field = member.getAnnotation(Field.class);
        if (field == null) {
          return null;
        }
        return field.value();
      }
      // Getter
      if (member instanceof AnnotatedMethod) {
        return member.getName();
      }
      return null;
    }

    @Override
    public boolean hasCreatorAnnotation(Annotated a) {
      final AnnotatedConstructor ctor = (AnnotatedConstructor) a;
      if (ctor.getParameterCount() == 0) {
        return true;
      }
      final Field field = ctor.getParameter(0).getAnnotation(Field.class);
      return field != null;
    }
  }

  /**
   * Instructs jackson to use {@link FoobarAbstractTypeResolver} and {@link FoobarAnnotationIntrospector}.
   */
  public static class FoobarModule extends SimpleModule {

    @Override
    public void setupModule(final SetupContext context) {
      context.addAbstractTypeResolver(new FoobarAbstractTypeResolver());
      context.appendAnnotationIntrospector(new FoobarAnnotationIntrospector());
    }
  }

  /**
   * The repro.
   */
  public static void main(final String... args) throws IOException {

    final ObjectMapper mapper = new ObjectMapper()
        .registerModule(new FoobarModule());

    final Foobar foobar = mapper.readValue("{\"bar\":\"bar\", \"foo\":\"foo\"}", Foobar.class);

    System.out.println(foobar);
  }
}
