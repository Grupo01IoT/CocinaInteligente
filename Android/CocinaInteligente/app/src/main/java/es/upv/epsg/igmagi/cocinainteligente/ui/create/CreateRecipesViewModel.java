package es.upv.epsg.igmagi.cocinainteligente.ui.create;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CreateRecipesViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public CreateRecipesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is send fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}