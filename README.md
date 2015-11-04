# web-client-oauth2-uaa

Para utilizar o UAA como Authorization Server, &eacute; preciso cadastrar a aplica&ccedil;&atilde;o
como um _client_ no UAA criando um client_id e um _client_secret_.

## UAA
Para instalar e executar um UAA localmente, siga as instru&ccedil;&otilde;es do site [GitHub CloudFoundry UAA](https://github.com/cloudfoundry/uaa).

### Selecionando o target do UAA
Pelo terminal, utilize o aplicativo de linha de comando para interagir com o UAA e selecione o servidor a ser utilizado.

> Para o servidor local, utilize o comando:

    $>uaac target http://localhost:8080/uaa

### Autenticando como admin
Em seguida, utilize o aplicativo para obter o _token_ para interagir como admnistrador do UAA.
> Para o servidor local o usu&aacute;rio admin vem configurado com a senha _adminsecret_.

    $>uaac token client get admin -s adminsecret

### Cadastrando um _client_
Para que uma aplica&ccedil;&atilde;o possa utilizar o UAA para autentica&ccedil;&atilde;o, primeiramente, deve-se cadastrar um _client_ no UAA informando:
* **client_id**: um identificador &uacute;nico que ser&aacute; utilizado para identificar a aplica&ccedil;&atilde;o.
* **secret**: uma senha a ser utilizada para validar o acesso.
* **redirect_uri**: uma URL com um endere&ccedil;o que ser&atilde; chamado pelo UAA para retornar os _tokens_ a serem utilizados na autentica&ccedil;&atilde;o.
* **scope**: os escopos utilizados para autentica&ccedil;&atilde;o (https://github.com/cloudfoundry/uaa/blob/master/docs/UAA-APIs.rst#scopes-authorized-by-the-uaa).
* **authorized_grant_types**: uma lista separada por v&iacute;rgulas dos tipos de permiss&otilde;es requeridas pela aplica&ccedil;&atilde;o. (https://github.com/cloudfoundry/uaa/blob/master/docs/UAA-Security.md#oauth-client-applications)
  * **client_credentials**
  * **password**
  * **implicit**
  * **refresh_token**
  * **authorization_code**
* **authorities**: lista de autoridades dadas ao cliente.
  * **uaa.resource**: autoridade necess&aacute;ria para obter a chave (_token_key_) de autentica&ccedil;&atilde;o utilizada pelo UAA para assinar o _id_token_.

Exemplo de comando para cadastrar uma aplica&ccedil;&atilde;o identificada por **myapp2**.

    $>uaac client add myapp2 \
      --secret myapp2clientsecret \
      --scope openid \
      --redirect_uri http://localhost:3030/callback \
      --authorized_grant_types "authorization_code, client_credentials" \
      --authorities uaa.resource

### Cadastrando usu&aacute;rios
De forma an&aacute;loga, &eacute; poss&iacute;vel cadastrar usu&aacute;rios no UAA pelo aplicativo de linha de comando.
Informando:
* **username**: o nome de usu&aacute;rio a ser utilizado para autentica&ccedil;&atilde;o do usu&aacute;rio.
* **password**: senha do usu&aacute;rio.
* **emails**: lista de e-mails v&aacute;lidos do usu&aacute;rio.
* **given_name**: Nome da pessoa.
* **family_name**: Sobrenome.
* **phones**: lista de n&uacute;meros de telefone.

Exemplo de comando para cadastrar o usu&aacute;rio **benkenobi**:

    $>uaac user add benkenobi \
      --password starwars \
      --emails ben.kenobi@jedi.tatooine.org \
      --given_name "Obi-Wan" \
      --family_name "Kenobi"

### Endpoints

* **authorization**: Endpoint para iniciar a autentica&ccedil;&atilde;o.
    http://localhost:8080/uaa/oauth/authorize
* **token**: Endpoint para obter o _access_token_ e o _id_token_.
    http://localhost:8080/uaa/oauth/token
* **token_key**: Endpoint para obter a chave _token_key_ utilizada pelo UAA para assinar o _id_token_.
    http://localhost:8080/uaa/token_key
* **user_info**: Endpoint para consultar as informa&ccedil;&otilde;es do usu&aacute;rio autenticado.
    http://localhost:8080/uaa/oauth/userinfo












