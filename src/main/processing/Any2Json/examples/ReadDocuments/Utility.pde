String[] listFileNames(String dir) {
  File file = new File(dir);
  if (file.isDirectory()) {
    String names[] = file.list();
    return names;
  } else {
    // If it's not a directory
    return null;
  }
}

void removeFileName(String filename1, String filename2) {
  File file1 = new File(filename1);
  File file2 = new File(filename2);
  file1.renameTo(file2);
}

void translateWord(String word) {
  try {
    link("https://translate.google.com/#view=home&op=translate&sl=auto&tl=en&text=" + java.net.URLEncoder.encode(word, "UTF-8"));
  } 
  catch (java.io.UnsupportedEncodingException e) {
    println(e);
  }
}
