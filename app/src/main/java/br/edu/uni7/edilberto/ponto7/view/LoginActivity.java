package br.edu.uni7.edilberto.ponto7.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import br.edu.uni7.edilberto.ponto7.R;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmpresa;
    private EditText etMatricula;
    private Button btEntrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Intent intent = getIntent();
        String empresa = intent.getStringExtra("empresa");
        String matricula = intent.getStringExtra("matricula");

        etEmpresa = findViewById(R.id.et_empresa);
        etEmpresa.setText(empresa);
        etMatricula = findViewById(R.id.et_matricula);
        etMatricula.setText(matricula);

        btEntrar = findViewById(R.id.bt_entrar);
        btEntrar.setOnClickListener(btEntrarOnClickListener);
    }

    public View.OnClickListener btEntrarOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("empresa", etEmpresa.getText().toString());
            resultIntent.putExtra("matricula", etMatricula.getText().toString());
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }
    };

    public void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

