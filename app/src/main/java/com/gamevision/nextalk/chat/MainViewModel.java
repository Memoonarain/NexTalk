package com.gamevision.nextalk.chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.gamevision.nextalk.models.UserModel;
import com.gamevision.nextalk.models.UserWithLastMessage;

import java.util.List;

public class MainViewModel extends ViewModel {
    private final MutableLiveData<List<UserWithLastMessage>> usersWithMessages = new MutableLiveData<>();
    private final UserRepository repository = new UserRepository();

    public LiveData<List<UserWithLastMessage>> getUsersWithMessages() {
        return usersWithMessages;
    }

    public void loadUsers() {
        repository.fetchUsersWithLastMessages(usersWithMessages);
    }
    private final MutableLiveData<List<UserModel>> searchResults = new MutableLiveData<>();

    public LiveData<List<UserModel>> getSearchResults() {
        return searchResults;
    }

    public void searchUsers(String query) {
        repository.searchUsers(query, new OnUsersFetchedCallback() {
            @Override
            public void onSuccess(List<UserModel> users) {
                searchResults.setValue(users);
            }

            @Override
            public void onFailure(String error) {
                // Handle error
            }
        });
    }
}
