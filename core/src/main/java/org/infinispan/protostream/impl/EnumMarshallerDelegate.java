package org.infinispan.protostream.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.infinispan.protostream.EnumMarshaller;
import org.infinispan.protostream.RawProtoStreamReader;
import org.infinispan.protostream.RawProtoStreamWriter;
import org.infinispan.protostream.UnknownFieldSet;
import org.infinispan.protostream.descriptors.EnumDescriptor;
import org.infinispan.protostream.descriptors.EnumValueDescriptor;
import org.infinispan.protostream.descriptors.FieldDescriptor;

/**
 * @author anistor@redhat.com
 * @since 1.0
 */
public final class EnumMarshallerDelegate<T extends Enum<T>> implements BaseMarshallerDelegate<T> {

   private final EnumMarshaller<T> enumMarshaller;

   private final Set<Integer> definedValues;

   EnumMarshallerDelegate(EnumMarshaller<T> enumMarshaller, EnumDescriptor enumDescriptor) {
      this.enumMarshaller = enumMarshaller;
      definedValues = new HashSet<>(enumDescriptor.getValues().size());
      for (EnumValueDescriptor evd : enumDescriptor.getValues()) {
         definedValues.add(evd.getNumber());
      }
   }

   @Override
   public EnumMarshaller<T> getMarshaller() {
      return enumMarshaller;
   }

   @Override
   public void marshall(FieldDescriptor fd, T value, ProtoStreamWriterImpl writer, RawProtoStreamWriter out) throws IOException {
      writeEnum(fd.getNumber(), value, out);
   }

   public void writeEnum(int fieldNumber, T value, RawProtoStreamWriter out) throws IOException {
      int enumValue = enumMarshaller.encode(value);
      if (!definedValues.contains(enumValue)) {
         throw new IllegalStateException("Undefined enum value " + enumValue + " for " + enumMarshaller.getTypeName());
      }
      out.writeEnum(fieldNumber, enumValue);
   }

   @Override
   public T unmarshall(FieldDescriptor fd, ProtoStreamReaderImpl reader, RawProtoStreamReader in) throws IOException {
      if (reader == null) {
         throw new IllegalStateException("reader is null");
      }

      final int expectedTag = fd.getWireTag();
      int enumValue;
      UnknownFieldSet unknownFieldSet = reader.getUnknownFieldSet();
      Object o = unknownFieldSet.consumeTag(expectedTag);
      if (o != null) {
         enumValue = ((Long) o).intValue();
      } else {
         while (true) {
            int tag = in.readTag();
            if (tag == 0) {
               return null;
            }
            if (tag == expectedTag) {
               enumValue = in.readEnum();
               break;
            }
            unknownFieldSet.readSingleField(tag, in);
         }
      }

      return readEnum(expectedTag, enumValue, unknownFieldSet);
   }

   public T readEnum(int expectedTag, int enumValue, UnknownFieldSet unknownFieldSet) {
      T decoded = enumMarshaller.decode(enumValue);

      if (decoded == null && unknownFieldSet != null) {
         // the enum value was not recognized by the EnumMarshaller so rather than discarding it we add it to the unknown
         unknownFieldSet.putVarintField(expectedTag, enumValue);
      }

      return decoded;
   }
}
