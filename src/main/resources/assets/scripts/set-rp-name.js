function setRpName(element) {
  if (element.checked) {
    document.getElementById("rp-name").value = element.value
  } else {
    document.getElementById("rp-name").value = ''
  }
}

document.getElementById('loa1-rp').addEventListener('click', setRpName(this));
document.getElementById('forceauthn-noc3-rp').addEventListener('click', setRpName(this));
document.getElementById('not-signed-by-hub-rp').addEventListener('click', setRpName(this));
document.getElementById('non-eidas-rp').addEventListener('click', setRpName(this));
