package ohm.softa.a07.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import ohm.softa.a07.api.OpenMensaAPI;
import ohm.softa.a07.model.Meal;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainController implements Initializable {

	private static final Logger logger = LogManager.getLogger(MainController.class);
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
	private final OpenMensaAPI openMensaAPI;
	// use annotation to tie to component in XML
	@FXML
	private Button btnRefresh;

	@FXML
	private ListView<Meal> mealsList;

	@FXML
	private Button btnClose;

	@FXML
	private CheckBox chkVegetarian;

	private ObservableList<Meal> observableList;

	public MainController(){
		HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
		loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
		OkHttpClient client = new OkHttpClient.Builder()
			.addInterceptor(loggingInterceptor)
			.build();

		Retrofit retrofit = new Retrofit.Builder()
			.addConverterFactory(GsonConverterFactory.create())
			.baseUrl("https://openmensa.org/api/v2/")
			.client(client)
			.build();

		openMensaAPI = retrofit.create(OpenMensaAPI.class);

	}
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// set the event handler (callback)
		btnRefresh.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				// create a new (observable) list and tie it to the view
				openMensaAPI.getMeals(dateFormat.format(new Date())).enqueue(new Callback<List<Meal>>() {
					@Override
					public void onResponse(Call<List<Meal>> call, Response<List<Meal>> response) {
						if(response.isSuccessful() && response.body() != null){
							Platform.runLater(() ->{
								observableList.clear();
								observableList.addAll(response.body());
							});
						}
					}

					@Override
					public void onFailure(Call<List<Meal>> call, Throwable t) {

					}
				});

			}
		});
		btnClose.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Platform.exit();
				System.exit(0);
			}
		});
		chkVegetarian.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				observableList = observableList.filtered
					(m -> (m.getCategory().toLowerCase(Locale.ROOT).equals("vegetarisch") || m.getCategory().toLowerCase(Locale.ROOT).equals("vegan")));
			}
		});
		observableList =mealsList.getItems();
	}
}
