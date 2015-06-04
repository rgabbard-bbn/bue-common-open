package com.bbn.bue.common.serialization.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.module.guice.GuiceAnnotationIntrospector;
import com.fasterxml.jackson.module.guice.GuiceInjectableValues;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.inject.Injector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.google.common.base.Preconditions.checkNotNull;

public final class JacksonSerializer {

  private final ObjectMapper mapper;

  private JacksonSerializer(ObjectMapper mapper) {
    this.mapper = checkNotNull(mapper);
  }

  public static JacksonSerializer forNormalJSON() {
    return json().build();
  }

  public static Builder json() {
    return Builder.forJSONFactory(new JsonFactory());
  }

  /**
   * @deprecated Prefer calling {@link Builder#prettyOutput()}.
   * @return
   */
  @Deprecated
  public static JacksonSerializer forPrettyJSON() {
    return json().prettyOutput().build();
  }

  /**
   * @deprecated Caching is diabled, so this is no longer necessary
   * @return
   */
  @Deprecated
  public static JacksonSerializer forNormalJSONUncached() {
    return forNormalJSON();
  }

  public static JacksonSerializer forSmile() {
    return Builder.forJSONFactory(new SmileFactory()).build();
  }

  public static Builder smile() {
    return Builder.forJSONFactory(new SmileFactory());
  }

  /**
   * @deprecated Caching is disabled, so this is no longer necessary
   * @return
   */
  @Deprecated
  public static JacksonSerializer forSmileUncached() {
    return forSmile();
  }


  public void serializeTo(final Object o, final ByteSink out) throws IOException {
    final RootObject rootObj = RootObject.forObject(o);
    final OutputStream bufStream = out.openBufferedStream();
    mapper.writeValue(bufStream, rootObj);
    bufStream.close();
  }

  public Object deserializeFrom(final ByteSource source) throws IOException {
    final InputStream srcStream = source.openStream();
    final RootObject rootObj = mapper.readValue(srcStream, RootObject.class);
    srcStream.close();
    return rootObj.object();
  }

  public String writeValueAsString(Object value) throws JsonProcessingException {
    return mapper.writeValueAsString(value);
  }

  public <T> T deserializeFromString(String content, Class<T> valueType) throws IOException {
    return mapper.readValue(content, valueType);
  }

  public static final class Builder {
    final ObjectMapper objectMapper;

    private Builder(ObjectMapper objectMapper) {
      this.objectMapper = checkNotNull(objectMapper);
    }

    private static Builder forJSONFactory(JsonFactory jsonFactory) {
      return new Builder(mapperFromJSONFactory(jsonFactory));
    }

    public Builder prettyOutput() {
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
      return this;
    }

    public Builder registerGuiceBindings(Injector injector) {
      // this is from jackson-module-guice's ObjectMapperModule.get()
      // we do this in a separate method because we do not currenly want to inject the ObjectMapper
      final GuiceAnnotationIntrospector guiceIntrospector = new GuiceAnnotationIntrospector();
      objectMapper.setInjectableValues(new GuiceInjectableValues(injector));
      objectMapper.setAnnotationIntrospectors(
          new AnnotationIntrospectorPair(
              guiceIntrospector, objectMapper.getSerializationConfig().getAnnotationIntrospector()
          ),
          new AnnotationIntrospectorPair(
              guiceIntrospector, objectMapper.getDeserializationConfig().getAnnotationIntrospector()
          )
      );
      return this;
    }

    public JacksonSerializer build() {
      return new JacksonSerializer(objectMapper);
    }

    private static ObjectMapper mapperFromJSONFactory(JsonFactory jsonFactory) {
      final ObjectMapper mapper = new ObjectMapper(jsonFactory);
      mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
      mapper.findAndRegisterModules();
      return mapper;
    }
  }

  private static final class RootObject {

    @JsonCreator
    public RootObject(@JsonProperty("obj") final Object obj) {
      this.obj = checkNotNull(obj);
    }

    public static RootObject forObject(final Object obj) {
      return new RootObject(obj);
    }

    @JsonProperty("obj")
    public Object object() {
      return obj;
    }

    private final Object obj;
  }
}
