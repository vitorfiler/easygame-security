package br.com.economizenergiasecurity.rest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.economizenergiasecurity.service.UserService;
import br.com.economizenergiasecurity.service.dto.PasswordChangeDTO;

@RestController
@RequestMapping("/oauth")
public class AccountResource {

    @Autowired
    private UserService userService;

    @PostMapping(path = "/account/change-password")
    public void changePassword(@RequestBody PasswordChangeDTO passwordChangeDTO) {
        if (!checkPasswordLength(passwordChangeDTO.getNewPassword())) {
            //TODO: AbstractThrowableProblem?
            throw new RuntimeException("Incorrect password");
        }
        userService.changePassword(passwordChangeDTO.getCurrentPassword(), passwordChangeDTO.getNewPassword());
    }

    private static boolean checkPasswordLength(String password) {
        //TODO: ManagedUserVM?
        return !StringUtils.isEmpty(password) &&
                password.length() >= 4 &&
                password.length() <= 100;
    }

}
