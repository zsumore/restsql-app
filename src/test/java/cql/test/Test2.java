package cql.test;

public class Test2 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String orderByStr="sn:desc;sum_r8:asc";
		StringBuffer buffer = new StringBuffer("");
		if (orderByStr.indexOf(";") > 0) {
			String[] list = orderByStr.trim().split(";");
			int size = list.length;
			for (int i = 0; i < size; i++) {
				if (i != 0) {
					buffer.append(",");
				}
				buffer.append(list[i].replaceAll(":", " "));
			}
		} else {
			buffer.append(orderByStr.replaceAll(":", " "));
		}
		
		System.out.println(buffer.toString());

	}

}
