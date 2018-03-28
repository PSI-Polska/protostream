package org.infinispan.protostream.descriptors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author anistor@redhat.com
 * @since 2.0
 */
public abstract class AnnotationElement {

   /**
    * On what kind of descriptor can we place that annotation?
    */
   public enum AnnotationTarget {
      MESSAGE, ENUM, FIELD
   }

   /**
    * What type is the attribute?
    */
   public enum AttributeType {
      IDENTIFIER, STRING, CHARACTER, BOOLEAN, INT, LONG, FLOAT, DOUBLE, ANNOTATION
   }

   public static final long UNKNOWN_POSITION = 0;

   private static final long LINESHIFT = 32;

   private static final long COLUMNMASK = (1L << LINESHIFT) - 1;

   /**
    * Text position, encoded in the form of a {@code long}. Upper half is the line number, lower half is the column.
    */
   public final long position;

   public static int line(long pos) {
      return (int) (pos >>> LINESHIFT);
   }

   public static int column(long pos) {
      return (int) (pos & COLUMNMASK);
   }

   public static long makePosition(int line, int column) {
      return ((long) line << LINESHIFT) + column;
   }

   public static String positionToString(long pos) {
      return line(pos) + "," + column(pos);
   }

   protected AnnotationElement(long position) {
      this.position = position;
   }

   public static abstract class Value extends AnnotationElement {

      protected Value(long pos) {
         super(pos);
      }

      public abstract Object getValue();
   }

   public static final class Annotation extends Value {

      /**
       * Name of 'default' attribute.
       */
      public static final String VALUE_DEFAULT_ATTRIBUTE = "value";

      private final String name;

      private final Map<String, Attribute> attributes;

      public Annotation(long pos, String name, Map<String, Attribute> attributes) {
         super(pos);
         this.name = name;
         this.attributes = attributes;
      }

      public String getName() {
         return name;
      }

      public Map<String, Attribute> getAttributes() {
         return attributes;
      }

      @Override
      public Annotation getValue() {
         return this;
      }

      public Value getDefaultAttributeValue() {
         return getAttributeValue(VALUE_DEFAULT_ATTRIBUTE);
      }

      public Value getAttributeValue(String attributeName) {
         Attribute attribute = attributes.get(attributeName);
         if (attribute == null) {
            throw new IllegalStateException("Attribute " + attributeName + " of annotation " + name + " is missing");
         }
         return attribute.value;
      }

      public void acceptVisitor(Visitor visitor) {
         visitor.visit(this);
      }
   }

   public static final class Attribute extends AnnotationElement {

      private final String name;

      private final Value value;

      public Attribute(long pos, String name, Value value) {
         super(pos);
         this.name = name;
         this.value = value;
      }

      public String getName() {
         return name;
      }

      public Value getValue() {
         return value;
      }

      public void acceptVisitor(Visitor visitor) {
         visitor.visit(this);
      }
   }

   /**
    * An identifier is a bit like a string literal but it does not have the quotation marks and it cannot contain
    * white space.
    */
   public static final class Identifier extends Value {

      private final String identifier;

      public Identifier(long pos, String identifier) {
         super(pos);
         this.identifier = identifier;
      }

      public String getIdentifier() {
         return identifier;
      }

      @Override
      public String getValue() {
         return identifier;
      }

      public void acceptVisitor(Visitor visitor) {
         visitor.visit(this);
      }
   }

   public static final class Array extends Value {

      private final List<Value> values;

      public Array(long pos, List<Value> values) {
         super(pos);
         this.values = values;
      }

      public List<Value> getValues() {
         return values;
      }

      @Override
      public List<Object> getValue() {
         List<Object> valueList = new ArrayList<>(values.size());
         for (Value val : values) {
            valueList.add(val.getValue());
         }
         return valueList;
      }

      public void acceptVisitor(Visitor visitor) {
         visitor.visit(this);
      }
   }

   /**
    * A constant value of type: {@link String}, {@link Character}, {@link Boolean} or {@link Number}.
    */
   public static final class Literal extends Value {

      private final Object value;

      public Literal(long pos, Object value) {
         super(pos);
         this.value = value;
      }

      @Override
      public Object getValue() {
         return value;
      }

      public void acceptVisitor(Visitor visitor) {
         visitor.visit(this);
      }
   }

   public void acceptVisitor(Visitor visitor) {
      visitor.visit(this);
   }

   public static abstract class Visitor {

      public void visit(Annotation tree) {
         visit((AnnotationElement) tree);
      }

      public void visit(Attribute tree) {
         visit((AnnotationElement) tree);
      }

      public void visit(Array tree) {
         visit((AnnotationElement) tree);
      }

      public void visit(Identifier tree) {
         visit((AnnotationElement) tree);
      }

      public void visit(Literal tree) {
         visit((AnnotationElement) tree);
      }

      public void visit(AnnotationElement annotationElement) {
         throw new IllegalStateException("Unexpected annotation element: " + annotationElement);
      }
   }
}
