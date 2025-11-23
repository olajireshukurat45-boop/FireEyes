package sms.com.sms.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import sms.com.sms.dto.UserDTO;
import sms.com.sms.model.Users;
import sms.com.sms.model.GasDetector;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Entity -> DTO mapping
    @Mapping(target = "phoneNumbers", source = "phonenumber")  // Map Users.phoneNumbers to UserDTO.phonenumber
    @Mapping(target = "macAddress", source = "gasDetectors", qualifiedByName = "mapDetectorsToMacs")
    UserDTO toDto(Users user);

    // DTO -> Entity mapping (ignore gasDetectors for manual handling)
    @Mapping(target = "phonenumber", source = "phoneNumbers") // Map UserDTO.phonenumber to Users.phoneNumbers
    @Mapping(target = "gasDetectors", ignore = true)
    Users toEntity(UserDTO dto);

    @Named("mapDetectorsToMacs")
    static Set<String> mapDetectorsToMacs(Set<GasDetector> detectors) {
        if (detectors == null) return null;
        return detectors.stream()
                .map(GasDetector::getMacAddress)
                .collect(Collectors.toSet());
    }
}
