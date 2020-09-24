package org.infinispan.protostream.impl.parser.mappers;

import static org.infinispan.protostream.impl.parser.mappers.Mappers.ENUM_LIST_MAPPER;
import static org.infinispan.protostream.impl.parser.mappers.Mappers.EXTEND_LIST_MAPPER;
import static org.infinispan.protostream.impl.parser.mappers.Mappers.MESSAGE_LIST_MAPPER;
import static org.infinispan.protostream.impl.parser.mappers.Mappers.OPTION_LIST_MAPPER;
import static org.infinispan.protostream.impl.parser.mappers.Mappers.filter;

import java.util.List;

import org.infinispan.protostream.descriptors.FileDescriptor;

import com.squareup.protoparser.EnumElement;
import com.squareup.protoparser.MessageElement;
import com.squareup.protoparser.ProtoFile;

/**
 * Mapper for high level ProtoFile to FileDescriptor.
 *
 * @author gustavonalle
 * @author anistor@redhat.com
 * @since 2.0
 */
public final class ProtofileMapper implements Mapper<ProtoFile, FileDescriptor> {

   @Override
   public FileDescriptor map(ProtoFile protoFile) {
      List<MessageElement> messageTypes = filter(protoFile.typeElements(), MessageElement.class);
      List<EnumElement> enumTypes = filter(protoFile.typeElements(), EnumElement.class);
      return new FileDescriptor.Builder()
            .withSyntax(map(protoFile.syntax()))
            .withName(protoFile.filePath())
            .withPackageName(protoFile.packageName())
            .withMessageTypes(MESSAGE_LIST_MAPPER.map(messageTypes))
            .withEnumTypes(ENUM_LIST_MAPPER.map(enumTypes))
            .withExtendDescriptors(EXTEND_LIST_MAPPER.map(protoFile.extendDeclarations()))
            .withOptions(OPTION_LIST_MAPPER.map(protoFile.options()))
            .withDependencies(protoFile.dependencies())
            .withPublicDependencies(protoFile.publicDependencies())
            .build();
   }

   private FileDescriptor.Syntax map(ProtoFile.Syntax syntax) {
      if (syntax == null) {
         return null;
      }
      if (syntax == ProtoFile.Syntax.PROTO_2) {
         return FileDescriptor.Syntax.PROTO2;
      }
      if (syntax == ProtoFile.Syntax.PROTO_3) {
         return FileDescriptor.Syntax.PROTO3;
      }
      throw new IllegalArgumentException("Unexpected syntax : " + syntax);
   }
}
