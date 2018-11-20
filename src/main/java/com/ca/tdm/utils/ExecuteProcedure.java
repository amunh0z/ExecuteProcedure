package com.ca.tdm.utils;

import com.grid_tools.products.datamasker.IMaskFunction;

import java.sql.*;

public class ExecuteProcedure implements IMaskFunction {
    @Override
    public Object mask(Object... args){
        String o_proposal = "";
        int ret_code;

        String originalValue = (String) args[0];

        if (null == originalValue) {
            return null;
        }

        // Proprietary validations and modifications
        if (originalValue.length() < 1) {
            return originalValue;
        }

        //Receive the database engine name.
        //It can be either SQLSERVER, ORACLE, MYSQL, DB2, POSTGRE or SYBASE
        String engine = (String) args[1];

        //get the connection string information, in the format [user name]/[password]@[host name]:[port number]:[database]
        //(in the case of Oracle, instead of database, use Service Name)
        //and split into [user name]/[password] and [host name]:[port number]
        //the code below is searching for the last occurrence of the '@' character,
        //because either the user name and password are allowed to have this character in their formation
        String connstring = (String) args[2];
        int count = connstring.lastIndexOf('@');
        String parts[] =  {connstring.substring(0, count), connstring.substring(count + 1)};

        //after the two parts are split, it is time to split the first into [user name] and [password]
        //and the second into [host name] and [port number]
        String credential[] = parts[0].split("/");
        String server[] = parts[1].split(":");

        //the variables are then arranged. it is valid to notice that as port is a number, it was converted to an integer
        //for the sake of this code execution, it makes no difference at all
        String username = credential[0];
        String password = credential[1];
        String hostname = server[0];
        int portnumber = Integer.valueOf(server[1]);
        String database = server[2];

        //the stored procedure being considered in this code has 1 input and 1 output parameters,
        //and the input parameter is an integer, while the output parameter is a long
        //that's the reason why the branch argument was cast into an integer
        int branch = Integer.valueOf((String) args[3]);
        String procedure = (String) args[4];

        //concatenate the connection string based on the information received earlier, for each database engine supported
        String url = "";
        switch (engine) {
            case "SQLSERVER":
                url = "jdbc:sqlserver://" + hostname + ":" + portnumber + ";DatabaseName=" + database;
                break;
            case "ORACLE":
                url = "jdbc:oracle:thin:@//" + hostname + ":" + portnumber + "/" + database;
                break;
            case "MYSQL":
                url = "jdbc:mysql://" + hostname + ":" + portnumber + "/" + database;
                break;
            case "DB2":
                url = "jdbc:db2://" + hostname + ":" + portnumber + "/" + database;
                break;
            case "POSTGRE":
                url = "jdbc:postgresql://" + hostname + ":" + portnumber + "/" + database;
                break;
            case "SYBASE":
                url = "jdbc:sybase:Tds:" + hostname + ":" + portnumber + "/" + database;
                break;
            default:
                return 0;
        }

        Connection conn = null;
        try {

            //Establish a connection, passing the connection string, user name and password
            conn = DriverManager.getConnection(url, username, password);

            //remember that the stored procedure using in this code uses two parameters: 1 input and 1 output
            //the input parameter receives an integer number
            //and the output parameter returns a numeric (long) value
            //as this stored procedure does not return a record set, we use executeUpdate,
            //otherwise we would use execute or executeQuery instead
            CallableStatement pstmt = conn.prepareCall("{call " + procedure + "(?,?)}");
            pstmt.setInt(1, branch);
            pstmt.registerOutParameter(2, Types.NUMERIC);
            pstmt.executeUpdate();

            o_proposal = String.valueOf(pstmt.getLong(2));

            pstmt.close();
            conn.close();
        }
        catch (SQLException e) {
            ret_code = e.getErrorCode();
            System.err.println(ret_code + e.getMessage());
        }

        return o_proposal;
    }
}
