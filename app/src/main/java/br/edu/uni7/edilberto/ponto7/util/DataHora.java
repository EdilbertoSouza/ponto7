package br.edu.uni7.edilberto.ponto7.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DataHora {

    public static String dataExtenso(Date data) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, new Locale("pt", "BR"));
        String dataExtenso = df.format(data);
        return dataExtenso;
    }

    public static Date dataAtual() {
        Date dataAtual = new Date();
        return dataAtual;
    }

    public static Date horaAtual() {
        Date horaAtual = Calendar.getInstance().getTime();
        return horaAtual;
    }

    public static String dtos(Date data) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.format(data);
    }

    public static String htos(Date hora) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(hora);
    }

    public static String saudacao() {
        String saudacao;
        Calendar cal = new GregorianCalendar();
        int hora = cal.get(Calendar.HOUR_OF_DAY);
        if(hora >= 1 && hora <= 12){
            saudacao = "Bom dia!";
        } else if(hora >= 13 && hora <= 18){
            saudacao = "Bom tarde!";
        } else {
            saudacao = "Bom noite!";
        }
        return saudacao;
    }

}