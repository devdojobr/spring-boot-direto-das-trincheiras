# Exercise 02 - @RequestParam, @PathVariable

Crie uma classe chamada Anime, dentro de um pacote chamado domain, com os seguintes atributos: long id, String name. Crie um método dentro de Anime que retorne uma lista "harcoded" de Animes;
Atualize o AnimeController para retornar uma lista de Anime, em seguida, crie outros dois métodos, um para filtrar pelo nome, usando @RequestParam, e o segundo, para retornar um Anime pelo id, usando @PathVariable.