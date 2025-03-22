package com.agonyforge.mud.demo.event.scheduled;

import com.agonyforge.mud.demo.model.impl.BannedUser;
import com.agonyforge.mud.demo.model.repository.BannedUsersRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class BanListener {

    final private BannedUsersRepository bannedUsersRepository;

    @Autowired
    public BanListener(BannedUsersRepository bannedUsersRepository) {
        this.bannedUsersRepository = bannedUsersRepository;
    }

    @Scheduled(cron = "0 * * * * ?")
    void checkBannedUsers(){
        List<BannedUser> bannedUsers = bannedUsersRepository.findNotPermanent();

        List<BannedUser> usersToUnban = new ArrayList<>();

        bannedUsers.forEach(bannedUser -> {
            Date currentDate = new Date();
            Date banTo = bannedUser.getBannedToDate();
            if (banTo != null && banTo.before(currentDate)){
                usersToUnban.add(bannedUser);
            }
        });

        bannedUsersRepository.deleteAll(usersToUnban);
    }
}
