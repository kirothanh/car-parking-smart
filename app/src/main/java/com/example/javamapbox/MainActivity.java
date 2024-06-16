package com.example.javamapbox;

import static com.mapbox.maps.plugin.animation.CameraAnimationsUtils.getCamera;
import static com.mapbox.maps.plugin.gestures.GesturesUtils.addOnMapClickListener;
import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;
import static com.mapbox.navigation.base.extensions.RouteOptionsExtensions.applyDefaultNavigationOptions;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.Bearing;
import com.mapbox.api.directions.v5.models.RouteOptions;
import com.mapbox.api.directions.v5.models.VoiceInstructions;
import com.mapbox.bindgen.Expected;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;

import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.EdgeInsets;
import com.mapbox.maps.MapView;
import com.mapbox.maps.QueriedFeature;
import com.mapbox.maps.RenderedQueryOptions;
import com.mapbox.maps.ScreenCoordinate;
import com.mapbox.maps.Style;
import com.mapbox.maps.ViewAnnotationOptions;
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationPluginImplKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.gestures.OnMapClickListener;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentConstants;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.generated.LocationComponentSettings;
import com.mapbox.maps.viewannotation.ViewAnnotationManager;
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions;
import com.mapbox.navigation.base.options.NavigationOptions;
import com.mapbox.navigation.base.route.NavigationRoute;
import com.mapbox.navigation.base.route.NavigationRouterCallback;
import com.mapbox.navigation.base.route.RouterFailure;
import com.mapbox.navigation.base.route.RouterOrigin;
import com.mapbox.navigation.base.trip.model.RouteProgress;
import com.mapbox.navigation.core.MapboxNavigation;
import com.mapbox.navigation.core.directions.session.RoutesObserver;
import com.mapbox.navigation.core.directions.session.RoutesUpdatedResult;
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter;
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp;
import com.mapbox.navigation.core.trip.session.LocationMatcherResult;
import com.mapbox.navigation.core.trip.session.LocationObserver;
import com.mapbox.navigation.core.trip.session.RouteProgressObserver;
import com.mapbox.navigation.core.trip.session.VoiceInstructionsObserver;
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer;
import com.mapbox.navigation.ui.maneuver.api.MapboxManeuverApi;
import com.mapbox.navigation.ui.maneuver.model.Maneuver;
import com.mapbox.navigation.ui.maneuver.model.ManeuverError;
import com.mapbox.navigation.ui.maneuver.view.MapboxManeuverView;
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider;
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi;
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView;
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions;
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi;
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView;
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions;
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineError;
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources;
import com.mapbox.navigation.ui.maps.route.line.model.RouteSetValue;
import com.mapbox.navigation.ui.voice.api.MapboxSpeechApi;
import com.mapbox.navigation.ui.voice.api.MapboxVoiceInstructionsPlayer;
import com.mapbox.navigation.ui.voice.model.SpeechAnnouncement;
import com.mapbox.navigation.ui.voice.model.SpeechError;
import com.mapbox.navigation.ui.voice.model.SpeechValue;
import com.mapbox.navigation.ui.voice.model.SpeechVolume;
import com.mapbox.navigation.ui.voice.view.MapboxSoundButton;
import com.mapbox.search.SearchOptions;
import com.mapbox.search.autocomplete.PlaceAutocomplete;
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion;
import com.mapbox.search.ui.adapter.autocomplete.PlaceAutocompleteUiAdapter;
import com.mapbox.search.ui.view.CommonSearchViewConfiguration;
import com.mapbox.search.ui.view.SearchResultsView;
import com.mapbox.turf.TurfMeasurement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlin.jvm.functions.Function1;

public class MainActivity extends AppCompatActivity {
    MapView mapView;
    MaterialButton setRoute;
    FloatingActionButton focusLocationBtn;
    private final NavigationLocationProvider navigationLocationProvider = new NavigationLocationProvider();
    private MapboxRouteLineView routeLineView;
    private MapboxRouteLineApi routeLineApi;
    private final LocationObserver locationObserver = new LocationObserver() {
        @Override
        public void onNewRawLocation(@NonNull Location location) {

        }

        @Override
        public void onNewLocationMatcherResult(@NonNull LocationMatcherResult locationMatcherResult) {
            Location location = locationMatcherResult.getEnhancedLocation();
            navigationLocationProvider.changePosition(location, locationMatcherResult.getKeyPoints(), null, null);
            if (focusLocation) {
                updateCamera(Point.fromLngLat(location.getLongitude(), location.getLatitude()), (double) location.getBearing());
            }
        }
    };
    private final RoutesObserver routesObserver = new RoutesObserver() {
        @Override
        public void onRoutesChanged(@NonNull RoutesUpdatedResult routesUpdatedResult) {
            routeLineApi.setNavigationRoutes(routesUpdatedResult.getNavigationRoutes(), new MapboxNavigationConsumer<Expected<RouteLineError, RouteSetValue>>() {
                @Override
                public void accept(Expected<RouteLineError, RouteSetValue> routeLineErrorRouteSetValueExpected) {
                    Style style = mapView.getMapboxMap().getStyle();
                    if (style != null) {
                        routeLineView.renderRouteDrawData(style, routeLineErrorRouteSetValueExpected);
                    }
                }
            });
        }
    };
    boolean focusLocation = true;
    private MapboxNavigation mapboxNavigation;
    private void updateCamera(Point point, Double bearing) {
        MapAnimationOptions animationOptions = new MapAnimationOptions.Builder().duration(1500L).build();
        CameraOptions cameraOptions = new CameraOptions.Builder().center(point).zoom(18.0).bearing(bearing).pitch(45.0)
                .padding(new EdgeInsets(1000.0, 0.0, 0.0, 0.0)).build();

        getCamera(mapView).easeTo(cameraOptions, animationOptions);
    }
    private final OnMoveListener onMoveListener = new OnMoveListener() {
        @Override
        public void onMoveBegin(@NonNull MoveGestureDetector moveGestureDetector) {
            focusLocation = false;
            getGestures(mapView).removeOnMoveListener(this);
            focusLocationBtn.show();
        }

        @Override
        public boolean onMove(@NonNull MoveGestureDetector moveGestureDetector) {
            return false;
        }

        @Override
        public void onMoveEnd(@NonNull MoveGestureDetector moveGestureDetector) {

        }
    };
    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            if (result) {
                Toast.makeText(MainActivity.this, "Permission granted! Restart this app", Toast.LENGTH_SHORT).show();
            }
        }
    });

    private MapboxSpeechApi speechApi;
    private MapboxVoiceInstructionsPlayer mapboxVoiceInstructionsPlayer;

    private MapboxNavigationConsumer<Expected<SpeechError, SpeechValue>> speechCallback = new MapboxNavigationConsumer<Expected<SpeechError, SpeechValue>>() {
        @Override
        public void accept(Expected<SpeechError, SpeechValue> speechErrorSpeechValueExpected) {
            speechErrorSpeechValueExpected.fold(new Expected.Transformer<SpeechError, Unit>() {
                @NonNull
                @Override
                public Unit invoke(@NonNull SpeechError input) {
                    mapboxVoiceInstructionsPlayer.play(input.getFallback(), voiceInstructionsPlayerCallback);
                    return Unit.INSTANCE;
                }
            }, new Expected.Transformer<SpeechValue, Unit>() {
                @NonNull
                @Override
                public Unit invoke(@NonNull SpeechValue input) {
                    mapboxVoiceInstructionsPlayer.play(input.getAnnouncement(), voiceInstructionsPlayerCallback);
                    return Unit.INSTANCE;
                }
            });
        }
    };

    private MapboxNavigationConsumer<SpeechAnnouncement> voiceInstructionsPlayerCallback = new MapboxNavigationConsumer<SpeechAnnouncement>() {
        @Override
        public void accept(SpeechAnnouncement speechAnnouncement) {
            speechApi.clean(speechAnnouncement);
        }
    };

    VoiceInstructionsObserver voiceInstructionsObserver = new VoiceInstructionsObserver() {
        @Override
        public void onNewVoiceInstructions(@NonNull VoiceInstructions voiceInstructions) {
            speechApi.generate(voiceInstructions, speechCallback);
        }
    };

    private boolean isVoiceInstructionsMuted = false;
    private PlaceAutocomplete placeAutocomplete;
    private SearchResultsView searchResultsView;
    private PlaceAutocompleteUiAdapter placeAutocompleteUiAdapter;
    private TextInputEditText searchET;
    private boolean ignoreNextQueryUpdate = false;
    private MapboxManeuverView mapboxManeuverView;
    private MapboxManeuverApi maneuverApi;
    private MapboxRouteArrowView routeArrowView;
    private MapboxRouteArrowApi routeArrowApi = new MapboxRouteArrowApi();
    private RouteProgressObserver routeProgressObserver = new RouteProgressObserver() {
        @Override
        public void onRouteProgressChanged(@NonNull RouteProgress routeProgress) {
            Style style = mapView.getMapboxMap().getStyle();
            if (style != null) {
                routeArrowView.renderManeuverUpdate(style, routeArrowApi.addUpcomingManeuverArrow(routeProgress));
            }

            maneuverApi.getManeuvers(routeProgress).fold(new Expected.Transformer<ManeuverError, Object>() {
                @NonNull
                @Override
                public Object invoke(@NonNull ManeuverError input) {
                    return new Object();
                }
            }, new Expected.Transformer<List<Maneuver>, Object>() {
                @NonNull
                @Override
                public Object invoke(@NonNull List<Maneuver> input) {
                    mapboxManeuverView.setVisibility(View.VISIBLE);
                    mapboxManeuverView.renderManeuvers(maneuverApi.getManeuvers(routeProgress));
                    return new Object();
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView);
        focusLocationBtn = findViewById(R.id.focusLocation);
        setRoute = findViewById(R.id.setRoute);
        mapboxManeuverView = findViewById(R.id.maneuverView);

        maneuverApi = new MapboxManeuverApi(new MapboxDistanceFormatter(new DistanceFormatterOptions.Builder(MainActivity.this).build()));
        routeArrowView = new MapboxRouteArrowView(new RouteArrowOptions.Builder(MainActivity.this).build());

        MapboxRouteLineOptions options = new MapboxRouteLineOptions.Builder(this).withRouteLineResources(new RouteLineResources.Builder().build())
                .withRouteLineBelowLayerId(LocationComponentConstants.LOCATION_INDICATOR_LAYER).build();
        routeLineView = new MapboxRouteLineView(options);
        routeLineApi = new MapboxRouteLineApi(options);

        speechApi = new MapboxSpeechApi(MainActivity.this, getString(R.string.mapbox_access_token), Locale.US.toLanguageTag());
        mapboxVoiceInstructionsPlayer = new MapboxVoiceInstructionsPlayer(MainActivity.this, Locale.US.toLanguageTag());

        NavigationOptions navigationOptions = new NavigationOptions.Builder(this).accessToken(getString(R.string.mapbox_access_token)).build();

        MapboxNavigationApp.setup(navigationOptions);
        mapboxNavigation = new MapboxNavigation(navigationOptions);

        mapboxNavigation.registerRouteProgressObserver(routeProgressObserver);
        mapboxNavigation.registerRoutesObserver(routesObserver);
        mapboxNavigation.registerLocationObserver(locationObserver);
        mapboxNavigation.registerVoiceInstructionsObserver(voiceInstructionsObserver);

        placeAutocomplete = PlaceAutocomplete.create(getString(R.string.mapbox_access_token));
        searchET = findViewById(R.id.searchET);

        searchResultsView = findViewById(R.id.search_results_view);
        searchResultsView.initialize(new SearchResultsView.Configuration(new CommonSearchViewConfiguration()));

        placeAutocompleteUiAdapter = new PlaceAutocompleteUiAdapter(searchResultsView, placeAutocomplete, LocationEngineProvider.getBestLocationEngine(MainActivity.this));

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (ignoreNextQueryUpdate) {
                    ignoreNextQueryUpdate = false;
                } else {
                    placeAutocompleteUiAdapter.search(charSequence.toString(), new Continuation<Unit>() {
                        @NonNull
                        @Override
                        public CoroutineContext getContext() {
                            return EmptyCoroutineContext.INSTANCE;
                        }

                        @Override
                        public void resumeWith(@NonNull Object o) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    searchResultsView.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        MapboxSoundButton soundButton = findViewById(R.id.soundButton);
        soundButton.unmute();
        soundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isVoiceInstructionsMuted = !isVoiceInstructionsMuted;
                if (isVoiceInstructionsMuted) {
                    soundButton.muteAndExtend(1500L);
                    mapboxVoiceInstructionsPlayer.volume(new SpeechVolume(0f));
                } else {
                    soundButton.unmuteAndExtend(1500L);
                    mapboxVoiceInstructionsPlayer.volume(new SpeechVolume(1f));
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                activityResultLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            activityResultLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
        } else {
            mapboxNavigation.startTripSession();
        }

        focusLocationBtn.hide();
        LocationComponentPlugin locationComponentPlugin = getLocationComponent(mapView);
        getGestures(mapView).addOnMoveListener(onMoveListener);

        setRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Please select a location in map", Toast.LENGTH_SHORT).show();
            }
        });



        mapView.getMapboxMap().loadStyleUri("mapbox://styles/biggoku123/clul176r9013y01pia0zk7p4j", new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                mapView.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(20.0).build());
                locationComponentPlugin.setEnabled(true);
                locationComponentPlugin.setLocationProvider(navigationLocationProvider);
                getGestures(mapView).addOnMoveListener(onMoveListener);
                locationComponentPlugin.updateSettings(new Function1<LocationComponentSettings, Unit>() {
                    @Override
                    public Unit invoke(LocationComponentSettings locationComponentSettings) {
                        locationComponentSettings.setEnabled(true);
                        locationComponentSettings.setPulsingEnabled(true);
                        return null;
                    }
                });

                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.location_pin);
                Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.location);
                Bitmap bitmap3 = BitmapFactory.decodeResource(getResources(), R.drawable.location_error);

                style.addImage("location_pin", bitmap);
                style.addImage("location", bitmap2);
                style.addImage("location_error", bitmap3);


                //tôi thêm
                // Khai báo và khởi tạo PointAnnotationManager một lần
                PointAnnotationManager pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(AnnotationPluginImplKt.getAnnotations(mapView), mapView);
                // Biến để theo dõi PointAnnotation cuối cùng từ click trên bản đồ
                final PointAnnotation[] lastClickedAnnotation = {null};
                //end tôi thêm

                addOnMapClickListener(mapView.getMapboxMap(), new OnMapClickListener() {
                    @Override
                    public boolean onMapClick(@NonNull Point point) {
                        //Toi them
                        // Xóa PointAnnotation trước đó được tạo từ click nếu có
                        if (lastClickedAnnotation[0] != null) {
                            pointAnnotationManager.delete(lastClickedAnnotation[0]);
                        }

                        // Tạo mới PointAnnotation cho vị trí click
                        PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions()
                                .withPoint(point)
                                .withIconImage("location_pin") // Đảm bảo tên icon chính xác và đã được đăng ký
                                .withTextAnchor(TextAnchor.CENTER);

                        // Lưu PointAnnotation mới vào biến lastClickedAnnotation
                        lastClickedAnnotation[0] = pointAnnotationManager.create(pointAnnotationOptions);

                        // Set route khi nhấn nút setRoute
                        setRoute.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                fetchRoute(point);
                            }
                        });
                        return true;
                        //end them

                    }
                });


                class LocationInfo {
                    String name;
                    int parkingSpots;
                    int parkingLimits;

                    public LocationInfo(String name, int parkingSpots, int parkingLimits) {
                        this.name = name;
                        this.parkingSpots = parkingSpots;
                        this.parkingLimits = parkingLimits;
                    }
                }

                // Doan toi them
                // Danh sách các điểm và thông tin tương ứng
                Map<Point, LocationInfo> pointDetails = new HashMap<>();
                pointDetails.put(Point.fromLngLat(105.850906, 21.170190), new LocationInfo("Điểm A", 50, 40));
                pointDetails.put(Point.fromLngLat(105.851135, 21.170176), new LocationInfo("Điểm B", 30, 30));
                pointDetails.put(Point.fromLngLat(105.851375, 21.170174), new LocationInfo("Điểm C", 40, 50));
                pointDetails.put(Point.fromLngLat(105.773898, 21.071582), new LocationInfo("Bãi gửi xe Đh Mỏ địa chất", 40, 40));
                pointDetails.put(Point.fromLngLat(105.777290, 21.075847), new LocationInfo("Bãi gửi xe Sân bóng Đức Thắng", 20, 15));
                pointDetails.put(Point.fromLngLat(105.776339, 21.071517), new LocationInfo("Bãi gửi xe BV Đa khoa Phương Đông", 40, 40));
                pointDetails.put(Point.fromLngLat(105.778483, 21.083175), new LocationInfo("Bãi gửi xe Đh Y Tế Công Cộng", 30, 20));
                pointDetails.put(Point.fromLngLat(105.789785, 21.050297), new LocationInfo("Bãi gửi xe bệnh viện E", 15, 30));


                // Tạo và thêm các PointAnnotation cho từng điểm
                Map<PointAnnotation, LocationInfo> annotationMap = new HashMap<>();
//                for (Map.Entry<Point, LocationInfo> entry : pointDetails.entrySet()) {
//                    PointAnnotationOptions options = new PointAnnotationOptions()
//                            .withPoint(entry.getKey())
//                            .withIconImage(entry.getValue().parkingSpots > entry.getValue().parkingLimits ? "location_error" : "location");
//                    PointAnnotation annotation = pointAnnotationManager.create(options);
//                    annotationMap.put(annotation, entry.getValue());
//                }

                for (Map.Entry<Point, LocationInfo> entry : pointDetails.entrySet()) {
                    Point point = entry.getKey();
                    LocationInfo info = entry.getValue();

                    // Kiểm tra điều kiện để chọn icon
                    Bitmap icon = info.parkingSpots > info.parkingLimits ?
                            BitmapFactory.decodeResource(getResources(), R.drawable.location_error) :
                            BitmapFactory.decodeResource(getResources(), R.drawable.location);

                    Bitmap combinedBitmap = createIconWithText(icon, info.name);
                    style.addImage(info.name, combinedBitmap);

                    PointAnnotationOptions options = new PointAnnotationOptions()
                            .withPoint(point)
                            .withIconImage(info.name);
                    PointAnnotation annotation = pointAnnotationManager.create(options);
                    annotationMap.put(annotation, info);
                }

                // Thêm listener cho click sự kiện trên các PointAnnotation
                pointAnnotationManager.addClickListener(annotation -> {
                    Location userLocation = navigationLocationProvider.getLastLocation();
                    LocationInfo info = annotationMap.get(annotation);
                    if (userLocation != null && info != null) {
                        Point userPoint = Point.fromLngLat(userLocation.getLongitude(), userLocation.getLatitude());
                        Point annotationPoint = annotation.getPoint();
                        double distance = TurfMeasurement.distance(userPoint, annotationPoint, "meters");
                        String parkingStatus = info.parkingSpots > info.parkingLimits ? "Hết chỗ để xe" : "có " + info.parkingSpots + " chỗ đỗ xe";

                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.custom_toast, null);

                        TextView text = layout.findViewById(R.id.custom_toast_text);
                        text.setText(info.name + " " + parkingStatus + " và cách bạn " + String.format("%.0fm", distance));

                        Toast toast = new Toast(getApplicationContext());
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(layout);
                        toast.show();
                    }
                    return true;
                });

                Toast.makeText(MainActivity.this, "Default marker should be visible now", Toast.LENGTH_LONG).show();
                // Doan toi them


                focusLocationBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        focusLocation = true;
                        getGestures(mapView).addOnMoveListener(onMoveListener);
                        focusLocationBtn.hide();
                    }
                });

                placeAutocompleteUiAdapter.addSearchListener(new PlaceAutocompleteUiAdapter.SearchListener() {
                    @Override
                    public void onSuggestionsShown(@NonNull List<PlaceAutocompleteSuggestion> list) {

                    }

                    @Override
                    public void onSuggestionSelected(@NonNull PlaceAutocompleteSuggestion placeAutocompleteSuggestion) {
                        ignoreNextQueryUpdate = true;
                        focusLocation = false;
                        searchET.setText(placeAutocompleteSuggestion.getName());
                        searchResultsView.setVisibility(View.GONE);

                        pointAnnotationManager.deleteAll();
                        PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions().withTextAnchor(TextAnchor.CENTER).withIconImage(bitmap)
                                .withPoint(placeAutocompleteSuggestion.getCoordinate());
                        pointAnnotationManager.create(pointAnnotationOptions);
                        updateCamera(placeAutocompleteSuggestion.getCoordinate(), 0.0);

                        setRoute.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                fetchRoute(placeAutocompleteSuggestion.getCoordinate());
                            }
                        });
                    }

                    @Override
                    public void onPopulateQueryClick(@NonNull PlaceAutocompleteSuggestion placeAutocompleteSuggestion) {
                        //queryEditText.setText(placeAutocompleteSuggestion.getName());
                    }

                    @Override
                    public void onError(@NonNull Exception e) {

                    }
                });
            }
        });
    }

    // Phương thức tạo Bitmap với cả icon và text
    private Bitmap createIconWithText(Bitmap icon, String text) {
        int width = Math.max(icon.getWidth(), (int) (text.length() * 20)); // Tăng chiều rộng bitmap nếu văn bản dài
        int height = icon.getHeight() + 50; // Tăng chiều cao bitmap để có chỗ cho văn bản

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(icon, (width - icon.getWidth()) / 2, 0, null); // Căn giữa biểu tượng

        Paint paint = new Paint();
        paint.setTextSize(30);
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.CENTER);

        Rect textBounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), textBounds);
        int textHeight = textBounds.height();
        canvas.drawText(text, width / 2, icon.getHeight() + textHeight + 10, paint); // Căn giữa văn bản và đặt nó dưới biểu tượng

        return result;
    }

    @SuppressLint("MissingPermission")
    private void fetchRoute(Point point) {
        LocationEngine locationEngine = LocationEngineProvider.getBestLocationEngine(MainActivity.this);
        locationEngine.getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
            @Override
            public void onSuccess(LocationEngineResult result) {
                Location location = result.getLastLocation();
                setRoute.setEnabled(false);
                setRoute.setText("Fetching route...");
                RouteOptions.Builder builder = RouteOptions.builder();
                Point origin = Point.fromLngLat(Objects.requireNonNull(location).getLongitude(), location.getLatitude());
                builder.coordinatesList(Arrays.asList(origin, point));
                builder.alternatives(false);
                builder.profile(DirectionsCriteria.PROFILE_DRIVING);
                builder.bearingsList(Arrays.asList(Bearing.builder().angle(location.getBearing()).degrees(45.0).build(), null));
                applyDefaultNavigationOptions(builder);

                mapboxNavigation.requestRoutes(builder.build(), new NavigationRouterCallback() {
                    @Override
                    public void onRoutesReady(@NonNull List<NavigationRoute> list, @NonNull RouterOrigin routerOrigin) {
                        mapboxNavigation.setNavigationRoutes(list);
                        focusLocationBtn.performClick();
                        setRoute.setEnabled(true);
                        setRoute.setText("Set route");
                    }

                    @Override
                    public void onFailure(@NonNull List<RouterFailure> list, @NonNull RouteOptions routeOptions) {
                        setRoute.setEnabled(true);
                        setRoute.setText("Set route");
                        Toast.makeText(MainActivity.this, "Route request failed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCanceled(@NonNull RouteOptions routeOptions, @NonNull RouterOrigin routerOrigin) {

                    }
                });
            }

            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapboxNavigation.onDestroy();
        mapboxNavigation.unregisterRoutesObserver(routesObserver);
        mapboxNavigation.unregisterLocationObserver(locationObserver);
    }
}