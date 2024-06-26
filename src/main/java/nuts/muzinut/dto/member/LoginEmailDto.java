package nuts.muzinut.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginEmailDto {

    @NotNull
    @Email
    private String email;

    @NotNull
    @Size(min = 3, max = 100)
    private String password;
}