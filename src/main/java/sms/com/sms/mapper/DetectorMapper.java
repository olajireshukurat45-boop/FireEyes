package sms.com.sms.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import sms.com.sms.dto.DetectorDTO;
import sms.com.sms.model.GasDetector;
import sms.com.sms.model.Users;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface DetectorMapper {

    // Entity -> DTO
    @Mapping(
        target = "phoneNumbers",
        source = "users",
        qualifiedByName = "mapUsersToPhoneNumbers"
    )
    DetectorDTO toDto(GasDetector detector);

    // DTO -> Entity (ignoring Users relationship to handle manually)
    @Mapping(target = "users", ignore = true)
    GasDetector toEntity(DetectorDTO dto);

    @Named("mapUsersToPhoneNumbers")
    static Set<String> mapUsersToPhoneNumbers(Set<Users> users) {
        if (users == null) return null;
        return users.stream()
                    .map(Users::getPhonenumber)
                    .collect(Collectors.toSet());
    }
}
