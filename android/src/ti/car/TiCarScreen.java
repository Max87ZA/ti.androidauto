package ti.car;

import static androidx.car.app.model.Action.BACK;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.CarToast;
import androidx.car.app.Screen;
import androidx.car.app.model.CarIcon;
import androidx.car.app.model.GridItem;
import androidx.car.app.model.GridTemplate;
import androidx.car.app.model.ItemList;
import androidx.car.app.model.ListTemplate;
import androidx.car.app.model.Tab;
import androidx.car.app.model.TabContents;
import androidx.car.app.model.TabTemplate;
import androidx.car.app.model.MessageTemplate;
import androidx.car.app.model.OnClickListener;
import androidx.car.app.model.Row;
import androidx.car.app.model.SectionedItemList;
import androidx.car.app.model.Template;
import androidx.core.graphics.drawable.IconCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.util.TiConvert;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TiCarScreen extends Screen {
    private static CarContext globalCarContext;
    private static final String LCAT = "TiCarModule";
    public TiCarScreen(CarContext carContext) {
        super(carContext);
        globalCarContext = carContext;
    }

    public static void createToast(String message) {
        CarToast.makeText(globalCarContext, message, CarToast.LENGTH_LONG).show();
    }

    @NonNull
    @Override
    public Template onGetTemplate() {
        // List of templates https://developers.google.com/cars/design/create-apps/apps-for-drivers/templates/overview

        HashMap listData = TiCarModule.listData;
        String templateType = TiConvert.toString(listData.get("type"), "list");

        if (templateType.equals("message")) {
            MessageTemplate template = new MessageTemplate
                    .Builder(TiConvert.toString(listData.get("text"), ""))
                    .setTitle(TiConvert.toString(listData.get("title"), ""))
                    .build();

            return template;
        }
        else if (templateType.equals("tab")) {
            try {
                JSONObject data = TiConvert.toJSON(listData);
                JSONArray tabs = data.getJSONArray("tabs");



                // Get the content for the first tab (for initialization)
                JSONObject firstTabData = tabs.getJSONObject(0);
                Template firstTabTemplate = createChildTemplate(firstTabData);

                // Create TabContents using the first tab's content
                TabContents firstTabContents = new TabContents.Builder(firstTabTemplate).build();

                // Initialize the TabTemplate.Builder with the first tab's content
                TabTemplate.Builder tabTemplateBuilder = new TabTemplate.Builder((TabTemplate.TabCallback) firstTabContents);


                // Loop through the tabs and add them one by one using addTab()
                for (int i = 0; i < tabs.length(); i++) {
                    JSONObject tabData = tabs.getJSONObject(i);
                    String tabTitle = tabData.getString("title");
                    CarIcon tabIcon = getIconFromResource(tabData.getString("image"));

                    // Create the Tab object
                    Tab tab = new Tab.Builder()
                            .setTitle(tabTitle)
                            .setIcon(tabIcon)
                            .build();

                    // Add each tab individually to the TabTemplate.Builder
                    tabTemplateBuilder.addTab(tab);
                }

                // Create content for the first tab
                Template currentTemplate = createChildTemplate(tabs.getJSONObject(0));  // Content for the first tab
                TabContents tabContents = new TabContents.Builder(currentTemplate).build();

                // Set the content for the selected tab
                tabTemplateBuilder.setTabContents(tabContents);

                // Optional back action
                tabTemplateBuilder.setHeaderAction(BACK);

                // Return the built TabTemplate
                return tabTemplateBuilder.build();

            } catch (Exception ex) {
                Log.e("TiCar", ex.toString());
                return new ListTemplate.Builder().build();  // Fallback in case of error
            }
        }





        else if (templateType.equals("grid")) {
            GridTemplate.Builder templateBuilder = new GridTemplate.Builder();
            try {
                JSONObject data = TiConvert.toJSON(listData);
                JSONArray sections = data.getJSONArray("sections");

                ItemList.Builder itemList = new ItemList.Builder();
                for (int sectionsId = 0; sectionsId < sections.length(); sectionsId++) {
                    JSONObject section = (JSONObject) sections.get(sectionsId);

                    String sectionTitle = section.getString("title");
                    JSONArray items = section.getJSONArray("items");

                    for (int i = 0; i < items.length(); ++i) {
                        JSONObject item = (JSONObject) items.get(i);
                        GridItem.Builder gridItem = new GridItem.Builder();
                        int finalI = i;
                        gridItem.setTitle(item.getString("text"))
                                .setImage(CarIcon.APP_ICON)
                                .setOnClickListener(() -> {
                                    Intent broadcastIntent = new Intent();
                                    broadcastIntent.setAction("click").putExtra("index", finalI);
                                    LocalBroadcastManager.getInstance(TiApplication.getAppCurrentActivity()).sendBroadcast(broadcastIntent);
                                });
                        itemList.addItem(gridItem.build());
                    }
                }
                return templateBuilder.setTitle(data.getString("title"))
                        .setHeaderAction(BACK)
                        .setSingleList(itemList.build())
                        .build();
            } catch (Exception ex) {
                Log.e("TiCar", ex.toString());
                return templateBuilder.setTitle("").build();
            }
        } else if (templateType.equals("list")) {
            ListTemplate.Builder templateBuilder = new ListTemplate.Builder();

            try {
                JSONObject data = TiConvert.toJSON(listData);
                JSONArray sections = data.getJSONArray("sections");

                for (int sectionsId = 0; sectionsId < sections.length(); sectionsId++) {
                    JSONObject section = (JSONObject) sections.get(sectionsId);

                    String sectionTitle = section.getString("title");
                    JSONArray items = section.getJSONArray("items");
                    ItemList.Builder radioList = new ItemList.Builder();

                    for (int i = 0; i < items.length(); ++i) {
                        JSONObject item = (JSONObject) items.get(i);
                        radioList.addItem(new Row.Builder().setTitle(item.getString("text")).build());
                    }

                    ItemList itemList = radioList.setOnSelectedListener(this::onSelected).build();
                    templateBuilder.addSectionedList(SectionedItemList.create(itemList, sectionTitle));
                }
                return templateBuilder.setTitle(data.getString("title")).setHeaderAction(BACK).build();
            } catch (Exception ex) {
                Log.e("TiCar", ex.toString());
                return templateBuilder.setTitle("").build();
            }
        } else {
            return new ListTemplate.Builder().build();
        }

    }

    private void onSelected(int i) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("click").putExtra("index", i);
        LocalBroadcastManager.getInstance(TiApplication.getAppCurrentActivity()).sendBroadcast(broadcastIntent);
    }
    public static Template createChildTemplate(JSONObject tabData) throws Exception {
        String type = tabData.getString("type");  // Get the template type (list, grid, etc.)

        switch (type) {
            case "list":
                return createListTemplate(tabData);  // Call a method to create ListTemplate
            case "grid":
                return createGridTemplate(tabData);  // Call a method to create GridTemplate
            case "message":
                return createMessageTemplate(tabData);  // Call a method to create MessageTemplate
            default:
                Log.e("TiCar", "Unknown template type: " + type);
                return new ListTemplate.Builder().build();  // Return an empty ListTemplate as fallback
        }
    }
    public static Template createListTemplate(JSONObject data) throws JSONException {
        ListTemplate.Builder templateBuilder = new ListTemplate.Builder();
        JSONArray sections = data.getJSONArray("sections");

        for (int sectionsId = 0; sectionsId < sections.length(); sectionsId++) {
            JSONObject section = sections.getJSONObject(sectionsId);
            String sectionTitle = section.getString("title");
            JSONArray items = section.getJSONArray("items");
            ItemList.Builder itemListBuilder = new ItemList.Builder();

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                itemListBuilder.addItem(new Row.Builder().setTitle(item.getString("text")).build());
            }

            ItemList itemList = itemListBuilder.build();
            templateBuilder.addSectionedList(SectionedItemList.create(itemList, sectionTitle));
        }

        return templateBuilder.setTitle(data.getString("title")).build();
    }
    public static Template createGridTemplate(JSONObject data) throws Exception {
        GridTemplate.Builder gridTemplateBuilder = new GridTemplate.Builder();
        JSONArray sections = data.getJSONArray("sections");

        ItemList.Builder itemListBuilder = new ItemList.Builder();
        for (int sectionId = 0; sectionId < sections.length(); sectionId++) {
            JSONObject section = sections.getJSONObject(sectionId);
            JSONArray items = section.getJSONArray("items");

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                GridItem.Builder gridItemBuilder = new GridItem.Builder();
                int finalI = i;
                gridItemBuilder.setTitle(item.getString("text"))
                        .setImage(CarIcon.APP_ICON)  // Set a default icon; modify as needed
                        .setOnClickListener(() -> {
                            // Handle item click
                            Intent broadcastIntent = new Intent("click");
                            broadcastIntent.putExtra("index", finalI);
                            LocalBroadcastManager.getInstance(TiApplication.getAppCurrentActivity()).sendBroadcast(broadcastIntent);
                        });

                itemListBuilder.addItem(gridItemBuilder.build());
            }
        }

        return gridTemplateBuilder
                .setTitle(data.getString("title"))
                .setHeaderAction(BACK)
                .setSingleList(itemListBuilder.build())
                .build();
    }
    public static Template createMessageTemplate(JSONObject data) throws Exception {
        return new MessageTemplate.Builder(data.getString("text"))
                .setTitle(data.getString("title"))
                .build();
    }
    private CarIcon getIconFromResource(String imageName) {
        try {
            // Get the application context
            Context context = TiApplication.getAppCurrentActivity();

            // Get the resource ID of the drawable by its name
            int resourceId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());

            if (resourceId != 0) {
                // Create a Drawable object from the resource ID
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    Drawable drawable = context.getDrawable(resourceId);
                }

                // Convert the Drawable into a CarIcon using IconCompat
                return new CarIcon.Builder(IconCompat.createWithResource(context, resourceId)).build();
            } else {
                Log.e(LCAT, "Icon resource not found: " + imageName);
            }
        } catch (Exception e) {
            Log.e(LCAT, "Error loading icon: " + e.toString());
        }

        // Return a fallback icon or null if the icon wasn't found
        return null;
    }
}
