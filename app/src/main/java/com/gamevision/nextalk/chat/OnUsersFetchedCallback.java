package com.gamevision.nextalk.chat;

import com.gamevision.nextalk.models.UserModel;

import java.util.List;

public interface OnUsersFetchedCallback {
    void onSuccess(List<UserModel> users);
    void onFailure(String errorMessage);
}
