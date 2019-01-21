package com.varamila.tk;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.shtrih.fiscalprinter.ShtrihFiscalPrinter;
import jpos.FiscalPrinter;
import jpos.JposException;
import org.springframework.http.MediaType;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/toSFPrinter")
public class KkmController {
    private static String companyName="";
    private static String companyAddress="";
    private static ShtrihFiscalPrinter printer;
    private static String comName = "ShtrihFptr";
    private static SimpleDateFormat df=new SimpleDateFormat("dd.MM.yyyy");
    private static SimpleDateFormat sdfCurrent=new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private static String folder=new File("").getAbsolutePath() + File.separator + "logs";
    private static String logFile=folder+File.separator+df.format(System.currentTimeMillis())+".log";
    private static String error="";
    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static final String PHONE_PATTERN = "[+]7\\d{10}";

    //инициализация наименования компании
    private static void setCompanyName() {
        companyName=companyAddress="";
        try {
            File file = ResourceUtils.getFile("classpath:companyName.txt");
            BufferedReader br = new BufferedReader( new InputStreamReader(
                    new FileInputStream(file), "UTF8"));
            ArrayList<String> list = new ArrayList<>();
            String line = null;
            while ((line = br.readLine()) != null) {
                list.add(line);
            }
            br.close();
            if (list.size()>=1) {
                companyName=list.get(0);
            }
            else
                writeLogString("Ошибка инициализации названия компании (tomcat home folder - companyName.txt - 1 строка)");
            if (list.size()>=2) {
                companyAddress=list.get(1);
            }
            else
                writeLogString("Ошибка инициализации адреса компании (tomcat home folder - companyName.txt - 2 строка)");

        }
        catch (Exception e) {
            writeLogString("Ошибка инициализации названия и адреса компании (tomcat home folder - companyName.txt - 1,2 строка)");
        }
        companyName=setCoolString(companyName);
        companyAddress=setCoolString(companyAddress);
    }
    //уточняем размер для красивой печати
    private static String setCoolString(String s) {
        if (s.length()!=0) {
            while (s.length() > 33)
                s = s.substring(0, s.length() - 1);
            int razn = 33 - s.length();
            if (razn > 0) {
                if (razn % 2 != 0) razn--;  //чётное
                int num = razn / 2;
                String space = "";
                for (int i = 0; i < num; i++) space = space + " ";
                s = space + s + space;
            }
        }
        return s;
    }

    //запись лога
    private static void writeLogString(String aMsg) {
        error=aMsg;
        try {
            File f0 = new File(folder);
            if (!f0.exists()) f0.mkdir();
            File f=new File(logFile);
            if(!f.exists()) f.createNewFile();
            String line=null;
            try (
                    InputStream fis = new FileInputStream(logFile);
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader br = new BufferedReader(isr);
            ) {
                line = br.readLine();
            }
            FileOutputStream fos=new FileOutputStream(logFile, true);
            byte[] b;
            if (line==null) b=("\r"+sdfCurrent.format(new java.util.Date()) +": "+aMsg).getBytes();
            else b=("\r\n"+sdfCurrent.format(new java.util.Date()) +": "+aMsg).getBytes();
            fos.write(b);
            fos.close();
        } catch (Exception e) {e.printStackTrace();}
    }

    //получаю объект класса
    private static Kkm getKkm(String json) {
        setCompanyName();
        writeLogString("Полученный json (" + json + ")");
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        Kkm k=null;
        try {
            k = gson.fromJson(json, Kkm.class);
        }
        catch (JsonSyntaxException e) {
            writeLogString("Не удалось распарсить json (" + e.getMessage() + ")");
        }
        return k;
    }

    //инициализирую ккм
    private static void initializePrinter() throws JposException {
        try {
            printer = new ShtrihFiscalPrinter(
                    new FiscalPrinter());
            printer.open(comName);
            printer.setPowerNotify(printer.JPOS_PN_ENABLED);
            FiscalPrinter p = new FiscalPrinter();
            printer.claim(0);
            printer.setDeviceEnabled(true);
            printer.resetPrinter();

            printer.setHeaderLine(1,companyName,false);
            printer.setHeaderLine(2,companyAddress,false);
        }
        catch (Exception e) {
            if (e.getMessage()!=null) writeLogString("ККМ не инициализирован (" + e.getMessage() + ")");
            else writeLogString("ККМ не инициализирован (" + e.toString() + ")");
        }
    }
    //задание имени кассира
    private static void SetKassir(String fio) {
        try {
            //если смена закрыта - по стандарту
            if (!printer.getDayOpened()) {
                if (fio!=null) {
                    printer.setPOSID("1", fio);
                    writeLogString("Задано имя кассира: '" + fio + "'");
                }
                else
                    writeLogString("Ошибка инициализации имени кассира: имя кассира null");
            }
            else {
                writeLogString("Смена не закрыта, имя кассира не меняем.");
            }
        }
        catch (Exception e) {
            if (fio!=null) writeLogString("Ошибка назначения ФИО кассира: '" + fio + "' (" + e.getMessage() + ")");
            else writeLogString("Ошибка назначения ФИО кассира: null (" + e.getMessage() + ")");
        }
    }

    //получаю json от медоса и печатаю чек продажи
    private static void Print(Kkm k) throws JposException, UnsupportedEncodingException {
        SetKassir(k.getFIO());

        String paymentType="0";
        if (k.getIsTerminalPayment()) paymentType="20";
        try {
            printer.beginFiscalReceipt(true);
            /*printer.printRecMessage(companyName);
            printer.printRecMessage("ДОБРО ПОЖАЛОВАТЬ!");*/

            try {
                if (k.getIsElectronic()) {
                    if (k.getCustomerPhone() != null && !k.getCustomerPhone().equals("") && k.getCustomerPhone().startsWith("+7"))
                        printer.fsWriteCustomerPhone(k.getCustomerPhone());
                    if (k.getCustomerPhone() != null && !k.getCustomerPhone().equals("") && k.getCustomerPhone().contains("@"))
                        printer.fsWriteCustomerEmail(k.getCustomerPhone());
                }
            }
            catch (Exception e) {
                writeLogString("Ошибка настройки телефона/email в чеке продажи (" + e.getMessage() + ")");
            }
        }
        catch (Exception e) {
            writeLogString("Ошибка инициации печати чека продажи (" + e.getMessage() + ")");
        }
        /* Price - стоимость позиции в копейках
           Quantity - количество в граммах (1 штука = 1000) - надо бы проверить!
           vatInfo - номер налога - ?
           unitPrice - цена за единицу товара
           unitName - название единицы товара
           printZReport - Day end required
        * */
        for (Position p : k.getPos()) {
            //writeLogString("Начаты позиции");
            if (p != null) { //тупо-криво, но gson ><
                try {
                    printer.printRecItem(p.getCode()+" " + p.getName(), p.getSum().multiply(new BigDecimal(100)).longValue(), p.getCount() * 1000, 0, p.getPrice().multiply(new BigDecimal(100)).longValue(), "шт.");
                    //printName("(" + p.getName() + ")");
                }
                catch (Exception e) {
                    writeLogString("Ошибка печати позиции в чеке продажи (" + e.getMessage() + ")");
                }
                try {
                    String str = "Ставка НДС " + p.getTaxName() + "%        =" + p.getTaxSum();
                    while (str.length() <= 35) str = str.replace(" =", "  =");
                    if (p.getTaxSum() != null && p.getTaxName().compareTo(BigDecimal.ZERO)!=0) printer.printRecMessage(str);
                }
                catch (Exception e) {
                    writeLogString("Ошибка печати ставки НДС в чеке продажи (" + e.getMessage() + ")");
                }
            }
            else {
                writeLogString("Исключительная ситуация: пустой или некорректный json");
            }
        }
        try {
            printer.printRecMessage("------------------------------------");
            String str = "Итого НДС " + "                   =" + k.getTotalTaxSum();
            while (str.length() <= 35) str = str.replace(" =", "  =");
            if (k.getTotalTaxSum() != null && k.getTotalTaxSum().compareTo(BigDecimal.ZERO)!=0) printer.printRecMessage(str);
        }
        catch (Exception e) {
            writeLogString("Ошибка печати ИТОГО в чеке продажи (" + e.getMessage() + ")");
        }
        try {
            printer.printRecTotal(k.getTotalPaymentSum().multiply(new BigDecimal(100)).longValue(), k.getTotalPaymentSum().multiply(new BigDecimal(100)).longValue(), paymentType);
        }
        catch (Exception e) {
            if (e.getMessage().toString().contains("Неверное состояние"))
                writeLogString("ККМ в неверном состоянии! Возможно, общая сумма по услугам не равна вычисленной! (" + e.getMessage() + ")");
            try {
                printer.printRecMessage("ККМ в неверном состоянии!");
            }
            catch (Exception e0) {
                writeLogString("Не удалось вывести сообщение на ККМ (о неверном состоянии) (" + e0.getMessage() + ")");
            }
        }
        try {
            printer.endFiscalReceipt(false);
        }
        catch (Exception e) {
            writeLogString("Ошибка завершения чека продажи (" + e.getMessage() + ")");
        }
    }

    //получаю сумму от медоса и печатаю чек возврата
    private void printRefund(Kkm k) throws JposException {
        SetKassir(k.getFIO());

        String paymentType = "0";
        if (k.getIsTerminalPayment()) paymentType = "20";
        BigDecimal toRefund = k.getTotalRefundSum();
        try {
            printer.beginFiscalReceipt(true);
        }
        catch (Exception e) {
            writeLogString("Ошибка инициации печати чека возврата (" + e.getMessage() + ")");
        }
        try {
            printer.printRecItemRefund("Все услуги", toRefund.multiply(new BigDecimal(100)).longValue(), 1000, 0, toRefund.multiply(new BigDecimal(100)).longValue(), "шт");
        }
        catch (Exception e) {
            writeLogString("Ошибка печати позиции в чеке возврата (" + e.getMessage() + ")");
        }
        try {
            printer.printRecTotal(toRefund.multiply(new BigDecimal(100)).longValue(), toRefund.multiply(new BigDecimal(100)).longValue(), paymentType);
        }
        catch (Exception e) {
            writeLogString("Ошибка ИТОГО в чеке возврата (" + e.getMessage() + ")");
        }
        try {
            printer.endFiscalReceipt(false);
        }
        catch (Exception e) {
            writeLogString("Ошибка завершения чека возврата (" + e.getMessage() + ")");
        }
    }
    //проверка на то, была ли уже попытка напечатать этот чек (true - была, false -  нет
    private boolean checkIfIdAccountFirstPrint(String idAc,String func) {
        try {
            File f0 = new File(logFile);
            if (!f0.exists()) return false;
            else {
                try (BufferedReader br = new BufferedReader(new FileReader(f0))) {
                    String sCurrentLine;
                    int cntPay=0, cntRefund=0;
                    while ((sCurrentLine = br.readLine()) != null) {
                        if (sCurrentLine.indexOf("Полученный id чека: " +idAc + " (makePayment). Отправлен на печать.") != -1) cntPay++;
                        if (sCurrentLine.indexOf("Полученный id чека: " +idAc + " (makeRefund). Отправлен на печать.") != -1) cntRefund++;
                    }
                    //Продан - может быть аннулирован
                    //Аннулирован - может быть продан
                    if (cntPay>cntRefund && !func.equals("makeRefund") || cntRefund>cntPay && !func.equals("makePayment")) return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    @GetMapping
    public String greeting() {
        return "Hello, Spring Boot!";
    }
    //@PostMapping("/print")
    @RequestMapping(value="/print", method=RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    public String answerMedOs(@RequestBody String data) throws JposException {
        try {
            System.out.println(data);
            Kkm k = getKkm(data);
            if (k!=null) {
                if (k.getGotId()!=null) {
                    if (checkIfIdAccountFirstPrint(k.getGotId(),k.getFunction())) {
                        writeLogString("Полученный id чека: " +k.getGotId() + " (" + k.getFunction() + ").");
                        writeLogString("Этот id чека: " +k.getGotId() + " уже был получен. Печатать повторно его нельзя.");
                        return "{status: \"error\",errorname: \"_errorname_\"}".replace("_errorname_",error);
                    }
                    else writeLogString("Полученный id чека: " +k.getGotId() + " (" + k.getFunction() + "). Отправлен на печать.");
                }
                else
                    writeLogString("Полученный id чека = null");
                if (k.getIsElectronic()) {
                    Pattern pattern = Pattern.compile(EMAIL_PATTERN);
                    if (k.getCustomerPhone()!=null && k.getCustomerPhone().contains("@") && !pattern.matcher(k.getCustomerPhone()).matches()) {
                        writeLogString("Получен некорректный email (" + k.getCustomerPhone() + ")");
                        return "{status: \"error\",errorname: \"_errorname_\"}".replace("_errorname_",error);
                    }
                    pattern = Pattern.compile(PHONE_PATTERN);
                    if (k.getCustomerPhone()!=null && !k.getCustomerPhone().contains("@") && !pattern.matcher(k.getCustomerPhone()).matches()) {
                        writeLogString("Получен некорректный номер телефона (" + k.getCustomerPhone() + ")");
                        return "{status: \"error\",errorname: \"_errorname_\"}".replace("_errorname_",error);
                    }
                }
                initializePrinter();
                switch (k.getFunction()) {
                    case "printXReport":
                        try {
                            printer.printRecMessage(companyName);
                            printer.printXReport();
                        }
                        catch (Exception e) {
                            writeLogString("Ошибка печати X-отчёта (" + e.getMessage() + ")");
                        }
                        break;
                    case "printZReport":
                        try {
                            printer.printRecMessage(companyName);
                            printer.printZReport();
                        }
                        catch (Exception e) {
                            writeLogString("Ошибка печати Z-отчёта (" + e.getMessage() + ")");
                        }
                        break;
                    case "makePayment":
                        try {
                            //подсчёт суммы
                            BigDecimal sum=BigDecimal.ZERO;
                            for (Position p : k.getPos()) {
                                if (p != null)   sum=sum.add(p.getSum());
                                BigDecimal price=p.getPrice();
                                if ((new BigDecimal(p.getCount()).multiply(price)).compareTo(p.getSum())!=0) {
                                    writeLogString("Сумма в позиции в чеке не равна рассчитанной (" + p.getCode() + ")");
                                    return "{status: \"error\",errorname: \"_errorname_\"}".replace("_errorname_",error);
                                }
                            }
                            if (sum.compareTo(k.getTotalPaymentSum())!=0)  writeLogString("Сумма по позициям в чеке не равна рассчитанной!");
                            else Print(k);
                        }
                        catch (Exception e) {
                            writeLogString("Ошибка печати чека продажи (" + e.getMessage() + ")");
                        }
                        break;
                    case "makeRefund":
                        try {
                            printRefund(k);
                        }
                        catch (Exception e) {
                            writeLogString("Ошибка печати чека возврата (" + e.getMessage() + ")");
                        }
                        break;
                    case "continuePrint":
                        try {
                            byte[] tx = "B0h".getBytes();  //команда возобновления печати при обрыве ленты посередине чека
                            printer.executeCommand(tx,0);
                        }
                        catch (Exception e) {
                            if (e.getMessage()!=null) writeLogString("getState: " + printer.getState() + ". Не удалось возобновить печать! (" + e.getMessage() + ")");
                            else writeLogString("getState: " + printer.getState() + ". Не удалось возобновить печать! (" + e.toString() + ")");
                        }
                        break;
                    default:
                        writeLogString("Была получена недекларированная команда (" + k.getFunction() + ")");
                        break;
                }
            }
        } catch (Exception e0) {
            writeLogString("Исключительная ситуация (" + e0.getMessage() + ")");
        }
        if (error.equals(""))  return "{status: \"ok\"}";
        else {
            if (error.toString().contains("Day end")) {
                printer.printRecMessage("24 часа истекли, нужен Z-отчёт");
                initializePrinter();
            }
            if (error.contains("Не хватает наличности")) {
                printer.printRecMessage("Не хватает наличности в кассе");
                initializePrinter();
            }
            return "{status: \"error\",errorName: \"_errorname_\"}".replace("_errorname_",error);
        }
    }
}