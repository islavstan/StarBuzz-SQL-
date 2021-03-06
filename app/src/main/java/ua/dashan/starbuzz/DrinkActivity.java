package ua.dashan.starbuzz;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DrinkActivity extends Activity {
    public final static String EXTRA_DRINK = "foodNo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink);

//Получение напитка из интента
//EXTRA_DRINKNO Идентификатор напитка, выбранного пользователем
        int drinkNo = (Integer) getIntent().getExtras().get(EXTRA_DRINK);
//Создание курсора
        try {
            //   Класс SQLiteOpenHelper упрощает задачи создания и сопровождения баз данных.
            //  Считайте, что это своего рода личный ассистент, который берет на себя
            // служебные операции по управлению базами данных.

            SQLiteOpenHelper starbuzzDatabaseHelper = new StarBuzzDataBaseHelper(this);
//этот метод возвращает объект типа SQLiteDatabase, который предоставляет доступ к базе данных
//Если вы собираетесь только читать информацию из базы данных, лучше использовать метод
            //getReadableDatabase(). Если же существует необходимость в записи в базу данных,
            // используйте метод getWritableDatabase().
            SQLiteDatabase db = starbuzzDatabaseHelper.getReadableDatabase();
//Создать курсор для получения из таблицы DRINK столбцов
//NAME, DESCRIPTION и IMAGE_RESOURCE_ID тех записей, у которых значение _id равно drinkNo
            Cursor cursor = db.query("DRINK", new String[]{"NAME", "DESCRIPTION", "IMAGE_RESOURCE_ID", "FAVORITE"}, "_id = ?",
                    new String[]{Integer.toString(drinkNo)}, null, null, null);

//Переход к первой записи в курсоре
//Курсор содержит всего одну запись, но и в этом случае переход необходим.
            if (cursor.moveToFirst()) {
                //Получение данных напитка из курсора
                //название напитка хранится в первом столбце курсора, описание —
//во втором,а идентификатор ресурса изображения —в третьем.
//Вспомните,что столбцыNAME,DESCRIPTION и IMAGE_RESOURCE_ID базы данных
//были включены в курсор именно в таком порядке
                String nameText = cursor.getString(0);
                String descriptionText = cursor.getString(1);
                int photoId = cursor.getInt(2);
                //Получить значение столбца FAVORITE. Оно хранится
                // в базе данных в числовом виде:1 — да, 0 — нет
                boolean isFavorite = (cursor.getInt(3) == 1);
//Заполнение названия напитка

                TextView name = (TextView) findViewById(R.id.name);
                name.setText(nameText);
//Заполнение описания напитка
                TextView description = (TextView) findViewById(R.id.description);
                description.setText(descriptionText);
//Заполнение изображения напитка
                ImageView photo = (ImageView) findViewById(R.id.photo);
                photo.setImageResource(photoId);
                photo.setContentDescription(nameText);
                //Заполнение флажка любимого напитка

                CheckBox favorite = (CheckBox) findViewById(R.id.favorite);
                favorite.setChecked(isFavorite);
//Закрыть курсор и базу данных
            }
            cursor.close();
            db.close();
        } catch (SQLiteException e) {
   /* Если будет выдано исключение SQLiteException,
значит, при работе с базой данных возникли
проблемы. В этом случае объект Toast
используется для выдачи сообщения
для пользователя.*/
            Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();

        }
    }
    //Обновление базы данных по щелчку на флажке
    public void onFavoriteClicked(View view) {
        int drinkNo = (Integer) getIntent().getExtras().get("foodNo");
        //в нашем приложении методу doInBackground() задачи AsyncTask должен передаваться напиток, выбранный пользователем
        /*Тип параметра, передаваемого методу execute(), должен соответствовать типу пара-
                метра, который ожидает получить метод doInBackground() объекта AsyncTask. Наш
        метод doInBackground() получает параметры типа Integer, поэтому передавать нужно
        целые числа*/
        new UpdateDrinkTask().execute(drinkNo);//Выполнить задачу AsyncTask и передать ей идентификатор напитка.
    }
//Внутренний класс для обновления напитка.
private class UpdateDrinkTask extends AsyncTask<Integer,Void,Boolean>{
    ContentValues drinkValues;
//Прежде чем запускать код базы данных на выполнение, добавить значение флажка
    //в объект ContentValues с именем drinkValues
    @Override
    protected void onPreExecute() {
        CheckBox favorite=(CheckBox)findViewById(R.id.favorite);
        drinkValues=new ContentValues();
        drinkValues.put("FAVORITE", favorite.isChecked());
    }
    //Код базы данных запускается на выполнение в фоновом потоке.
    @Override
    protected Boolean doInBackground(Integer... params) {//Код базы данных запускается на выполнение в фоновом потоке.
        int drinkNo = params[0];
        SQLiteOpenHelper starbuzzDatabaseHelper =
                new StarBuzzDataBaseHelper(DrinkActivity.this);
        try {
            SQLiteDatabase db = starbuzzDatabaseHelper.getWritableDatabase();
            db.update("DRINK", drinkValues,
                    "_id = ?", new String[] {Integer.toString(drinkNo)});
            db.close();
            return true;
        } catch(SQLiteException e) {
            return false;
        }
    }
    protected void onPostExecute(Boolean success) {
        if (!success) {
            Toast toast = Toast.makeText(DrinkActivity.this,
                    "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
}


















      /*  CheckBox favorite=(CheckBox)findViewById(R.id.favorite);
        ContentValues drinkValues=new ContentValues();
       // Значение флажка добавляется в объект ContentValues с именем drinkValues.
        drinkValues.put("FAVORITE",favorite.isChecked());
        SQLiteOpenHelper sqLiteOpenHelper=new StarBuzzDataBaseHelper(this);
        try{
            SQLiteDatabase db=sqLiteOpenHelper.getWritableDatabase();
            //обновить столбец FAVORITE текущим значением флажка
            db.update("DRINK"//имя таблицы, в которую вносятся изменения
                    ,drinkValues,  //объект ContentValues с парами «имя/значение» обновляемых столбцов и значений, которые им присваиваются
                    "_id = ?", //где id равно объекту который передал drinkNo
                    new String[] {Integer.toString(drinkNo)});
            db.close();
        } catch(SQLiteException e) {
            Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();

    }
}}*/

























  /*  @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink);
        int drinkNo=(Integer)getIntent().getExtras().get(EXTRA_DRINK);
        Drink drink=Drink.drinks[drinkNo];
        ImageView photo=(ImageView)findViewById(R.id.photo);
        photo.setImageResource(drink.getImageResourceId());
        photo.setContentDescription(drink.getName());
        //Заполнение названия напитка
        TextView name = (TextView)findViewById(R.id.name);
        name.setText(drink.getName());
        //Заполнение описания напитка
        TextView description = (TextView)findViewById(R.id.description);
        description.setText(drink.getDescription());
        //цена
        TextView cost=(TextView)findViewById(R.id.cost);
        cost.setText("Цена: "+drink.getCost()+"грн");
    }
}
*/